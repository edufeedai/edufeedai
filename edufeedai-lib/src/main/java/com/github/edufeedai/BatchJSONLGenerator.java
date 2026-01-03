/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.edufeedai.model.Digest;
import com.github.edufeedai.model.openai.platform.JSONLine;
import com.github.edufeedai.model.openai.platform.response.Body;
import com.github.edufeedai.model.openai.platform.response.Message;
import com.google.gson.Gson;

/**
 * Generates JSONL files for OpenAI Batch API with embedded file content.
 * Extracts text content from files and embeds it directly in the message payload
 * instead of using file attachments (which are not supported by Batch API).
 */
public class BatchJSONLGenerator {

    private static final Logger logger = LoggerFactory.getLogger(BatchJSONLGenerator.class);

    private final Connection connection;
    private final int taskId;
    private final String taskDirectory;
    private final String instructions;
    private final Digest digest;
    private final Gson gson;

    /**
     * Constructs a BatchJSONLGenerator.
     *
     * @param connection database connection
     * @param taskId the task ID to generate JSONL for
     * @param taskDirectory the directory where the JSONL file will be saved
     * @param instructions the system instructions for the correction prompt
     * @param digest the digest algorithm to use for generating submission IDs
     */
    public BatchJSONLGenerator(Connection connection, int taskId, String taskDirectory, String instructions, Digest digest) {
        this.connection = connection;
        this.taskId = taskId;
        this.taskDirectory = taskDirectory;
        this.instructions = instructions;
        this.digest = digest;
        // No incluir campos nulos en el JSON (comportamiento por defecto de Gson, importante para la API de OpenAI)
        this.gson = new Gson();
    }

    /**
     * Generates a single JSONL file with all submissions for the task.
     * The JSONL file will be created in the task directory.
     *
     * @return the path to the generated JSONL file
     * @throws IOException if file writing fails
     * @throws SQLException if database access fails
     */
    public String generateJsonl() throws IOException, SQLException {
        logger.info("Generating JSONL file for task {} in directory {}", taskId, taskDirectory);

        List<SubmissionData> submissions = fetchSubmissionsWithFiles();

        if (submissions.isEmpty()) {
            logger.warn("No submissions found with uploaded files for task {}", taskId);
            throw new IOException("No submissions with uploaded files found for task " + taskId);
        }

        // Obtener moodle_task_id si existe, sino usar task_id
        String fileName = getMoodleTaskIdOrDefault();
        String jsonlFilePath = taskDirectory + File.separator + fileName + ".jsonl";
        logger.debug("JSONL file will be saved as: {}", jsonlFilePath);

        StringBuilder jsonlContent = new StringBuilder();

        for (SubmissionData submission : submissions) {
            JSONLine jsonLine = buildJSONLine(submission);
            String json = gson.toJson(jsonLine);
            // Remove pretty printing for JSONL format (one line per entry)
            json = json.replaceAll("\\s+", " ").trim();
            jsonlContent.append(json).append(System.lineSeparator());
        }

        Files.write(Paths.get(jsonlFilePath), jsonlContent.toString().getBytes());
        logger.info("JSONL file generated successfully: {}", jsonlFilePath);

        return jsonlFilePath;
    }

    /**
     * Gets the Moodle task ID from the database, or falls back to the local task ID.
     *
     * @return the Moodle task ID if available, otherwise the task ID as a string
     * @throws SQLException if database access fails
     */
    private String getMoodleTaskIdOrDefault() throws SQLException {
        String query = "SELECT moodle_task_id FROM tasks WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, taskId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String moodleTaskId = rs.getString("moodle_task_id");
                    if (moodleTaskId != null && !moodleTaskId.isEmpty()) {
                        logger.debug("Using Moodle task ID as filename: {}", moodleTaskId);
                        return moodleTaskId;
                    }
                }
            }
        }

        // Fallback to local task ID
        logger.debug("Moodle task ID not available, using local task ID: {}", taskId);
        return String.valueOf(taskId);
    }

    /**
     * Fetches all submissions for the task with their extracted content from the database.
     *
     * @return list of submission data
     * @throws SQLException if database access fails
     */
    private List<SubmissionData> fetchSubmissionsWithFiles() throws SQLException {
        List<SubmissionData> submissions = new ArrayList<>();

        String query =
            "SELECT s.id, s.student_name, sf.file_name, sf.content_extracted " +
            "FROM submissions s " +
            "INNER JOIN submission_files sf ON s.id = sf.submission_id " +
            "WHERE s.task_id = ? AND sf.content_extracted IS NOT NULL " +
            "ORDER BY s.id, sf.id";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, taskId);

            try (ResultSet rs = stmt.executeQuery()) {
                int currentSubmissionId = -1;
                SubmissionData currentSubmission = null;

                while (rs.next()) {
                    int submissionId = rs.getInt("id");
                    String studentName = rs.getString("student_name");
                    String fileName = rs.getString("file_name");
                    String content = rs.getString("content_extracted");

                    if (submissionId != currentSubmissionId) {
                        // New submission
                        if (currentSubmission != null) {
                            submissions.add(currentSubmission);
                        }
                        currentSubmission = new SubmissionData(submissionId, studentName);
                        currentSubmissionId = submissionId;
                    }

                    if (currentSubmission != null && content != null && !content.isEmpty()) {
                        currentSubmission.addFileContent(fileName, content);
                    }
                }

                // Add the last submission
                if (currentSubmission != null) {
                    submissions.add(currentSubmission);
                }
            }
        }

        logger.debug("Fetched {} submissions with content for task {}", submissions.size(), taskId);
        return submissions;
    }

    /**
     * Builds a JSONLine object for a submission with embedded content.
     *
     * @param submission the submission data
     * @return JSONLine object ready for serialization
     */
    private JSONLine buildJSONLine(SubmissionData submission) {
        JSONLine jsonLine = new JSONLine();

        // Generar submission_id usando el hash del nombre del estudiante
        String submissionId;
        try {
            submissionId = digest.digest(submission.studentName);
        } catch (DigestException e) {
            logger.error("Error generating submission ID for student {}: {}", submission.studentName, e.getMessage());
            submissionId = "error_" + submission.submissionId;
        }

        jsonLine.setCustom_id(submissionId);
        jsonLine.setMethod("POST");
        jsonLine.setUrl("/v1/chat/completions");

        Body body = new Body();
        body.setModel("gpt-4o");

        Message[] messages = new Message[2];

        // System message with instructions
        messages[0] = new Message();
        messages[0].setRole("system");
        messages[0].setContent(instructions);

        // User message with embedded file content
        messages[1] = new Message();
        messages[1].setRole("user");

        // Construir mensaje concatenando todo el contenido de los archivos
        StringBuilder contentBuilder = new StringBuilder();
    
        contentBuilder.append("=== CONTENIDO DE LA ENTREGA ===\n\n");

        for (SubmissionData.FileContent fileContent : submission.fileContents) {
            contentBuilder.append(">>> Archivo: ").append(fileContent.fileName).append("\n");
            contentBuilder.append(fileContent.content).append("\n");
            contentBuilder.append("<<< Fin de: ").append(fileContent.fileName).append("\n\n");
        }

        messages[1].setContent(contentBuilder.toString());

        body.setMessages(messages);
        jsonLine.setBody(body);

        return jsonLine;
    }

    /**
     * Internal class to hold submission data with extracted file content.
     */
    private static class SubmissionData {
        final int submissionId;
        final String studentName;
        final List<FileContent> fileContents;

        SubmissionData(int submissionId, String studentName) {
            this.submissionId = submissionId;
            this.studentName = studentName;
            this.fileContents = new ArrayList<>();
        }

        void addFileContent(String fileName, String content) {
            this.fileContents.add(new FileContent(fileName, content));
        }

        /**
         * Internal class to hold file name and content.
         */
        static class FileContent {
            final String fileName;
            final String content;

            FileContent(String fileName, String content) {
                this.fileName = fileName;
                this.content = content;
            }
        }
    }
}

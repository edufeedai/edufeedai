/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;

/**
 * Gestor de la estructura de directorios del workspace.
 * Maneja la creación y acceso a directorios de tareas y entregas.
 */
public class WorkspaceManager {

    private static final String TASK_METADATA_FILE = ".task-metadata.json";
    private static final String SUBMISSION_METADATA_FILE = ".submission-metadata.json";

    private final Path workspaceRoot;
    private final Gson gson;

    public WorkspaceManager(String workspaceRoot) {
        this.workspaceRoot = Paths.get(workspaceRoot).toAbsolutePath();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public WorkspaceManager(Path workspaceRoot) {
        this.workspaceRoot = workspaceRoot.toAbsolutePath();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Obtiene el directorio de una tarea.
     * Formato: task_{id:03d}_{name_normalized}
     */
    public Path getTaskDirectory(int taskId, String taskName) {
        String normalizedName = normalizeDirectoryName(taskName);
        String dirName = String.format("task_%03d_%s", taskId, normalizedName);
        return workspaceRoot.resolve(dirName);
    }

    /**
     * Obtiene el directorio de una entrega.
     * Formato: submission_{num:02d}_{student_normalized}
     */
    public Path getSubmissionDirectory(int taskId, String taskName, String studentName, int submissionNumber) {
        Path taskDir = getTaskDirectory(taskId, taskName);
        String normalizedStudent = normalizeDirectoryName(studentName);
        String dirName = String.format("submission_%02d_%s", submissionNumber, normalizedStudent);
        return taskDir.resolve(dirName);
    }

    /**
     * Crea la estructura de directorios para una tarea y guarda sus metadatos.
     */
    public void createTaskStructure(Task task) throws IOException {
        Path taskDir = getTaskDirectory(task.getId(), task.getName());
        Files.createDirectories(taskDir);

        // Crear metadatos de la tarea
        TaskMetadata metadata = new TaskMetadata(
            task.getId(),
            task.getName(),
            task.getGradingConfigId(),
            task.getCreatedAt()
        );

        writeTaskMetadata(taskDir, metadata);
    }

    /**
     * Crea la estructura de directorios para una entrega y guarda sus metadatos.
     */
    public void createSubmissionStructure(int taskId, String taskName, Submission submission) throws IOException {
        Path submissionDir = getSubmissionDirectory(
            taskId,
            taskName,
            submission.getStudentName(),
            submission.getSubmissionNumber()
        );
        Files.createDirectories(submissionDir);

        // Crear metadatos de la entrega
        SubmissionMetadata metadata = new SubmissionMetadata(
            submission.getId(),
            submission.getStudentName(),
            submission.getSubmissionNumber(),
            submission.getSubmittedAt()
        );

        writeSubmissionMetadata(submissionDir, metadata);
    }

    /**
     * Lee los metadatos de una tarea desde el archivo .task-metadata.json
     */
    public TaskMetadata readTaskMetadata(Path taskDir) throws IOException {
        File metadataFile = taskDir.resolve(TASK_METADATA_FILE).toFile();
        if (!metadataFile.exists()) {
            throw new IOException("Task metadata file not found: " + metadataFile.getAbsolutePath());
        }

        try (FileReader reader = new FileReader(metadataFile)) {
            return gson.fromJson(reader, TaskMetadata.class);
        }
    }

    /**
     * Lee los metadatos de una entrega desde el archivo .submission-metadata.json
     */
    public SubmissionMetadata readSubmissionMetadata(Path submissionDir) throws IOException {
        File metadataFile = submissionDir.resolve(SUBMISSION_METADATA_FILE).toFile();
        if (!metadataFile.exists()) {
            throw new IOException("Submission metadata file not found: " + metadataFile.getAbsolutePath());
        }

        try (FileReader reader = new FileReader(metadataFile)) {
            return gson.fromJson(reader, SubmissionMetadata.class);
        }
    }

    /**
     * Escribe los metadatos de una tarea en el archivo .task-metadata.json
     */
    public void writeTaskMetadata(Path taskDir, TaskMetadata metadata) throws IOException {
        File metadataFile = taskDir.resolve(TASK_METADATA_FILE).toFile();
        try (FileWriter writer = new FileWriter(metadataFile)) {
            gson.toJson(metadata, writer);
        }
    }

    /**
     * Escribe los metadatos de una entrega en el archivo .submission-metadata.json
     */
    public void writeSubmissionMetadata(Path submissionDir, SubmissionMetadata metadata) throws IOException {
        File metadataFile = submissionDir.resolve(SUBMISSION_METADATA_FILE).toFile();
        try (FileWriter writer = new FileWriter(metadataFile)) {
            gson.toJson(metadata, writer);
        }
    }

    /**
     * Normaliza un nombre para usarlo en un nombre de directorio.
     * - Convierte a minúsculas
     * - Elimina acentos
     * - Reemplaza espacios por guiones bajos
     * - Elimina caracteres especiales
     */
    private String normalizeDirectoryName(String name) {
        if (name == null || name.isEmpty()) {
            return "unnamed";
        }

        // Convertir a minúsculas
        String normalized = name.toLowerCase();

        // Eliminar acentos
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");

        // Reemplazar espacios y caracteres especiales por guiones bajos
        normalized = normalized.replaceAll("[\\s]+", "_");
        normalized = normalized.replaceAll("[^a-z0-9_-]", "");

        // Eliminar guiones bajos múltiples
        normalized = normalized.replaceAll("_+", "_");

        // Eliminar guiones bajos al inicio y final
        normalized = normalized.replaceAll("^_|_$", "");

        // Limitar longitud
        if (normalized.length() > 50) {
            normalized = normalized.substring(0, 50);
        }

        return normalized.isEmpty() ? "unnamed" : normalized;
    }

    /**
     * Verifica si un directorio es un directorio de tarea válido.
     */
    public boolean isTaskDirectory(Path dir) {
        return Files.isDirectory(dir) &&
               dir.getFileName().toString().startsWith("task_") &&
               Files.exists(dir.resolve(TASK_METADATA_FILE));
    }

    /**
     * Verifica si un directorio es un directorio de entrega válido.
     */
    public boolean isSubmissionDirectory(Path dir) {
        return Files.isDirectory(dir) &&
               dir.getFileName().toString().startsWith("submission_") &&
               Files.exists(dir.resolve(SUBMISSION_METADATA_FILE));
    }

    public Path getWorkspaceRoot() {
        return workspaceRoot;
    }
}

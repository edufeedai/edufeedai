/*
 * EduFeedAi is an automated system for retrieving, evaluating, and generating AI-based feedback for student submissions in vocational education.
 *
 * Copyright (C) 2026 Arturo Candela
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.edufeedai;

import com.github.edufeedai.model.SubmissionIdMap;
import com.github.edufeedai.model.openai.platform.response.BatchRequest;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFeedbackPackager {

    final String assessmentFolder;
    final String submissionIdMapFile;
    final String feedbackFile;
    final String zipFileName;


    public ZipFeedbackPackager(String assessmentFolder, String submissionIdMapFile, String feedbackFile, String zipFileName) {
        // Constructor logic if needed
        this.assessmentFolder = assessmentFolder;
        this.submissionIdMapFile = submissionIdMapFile;
        this.feedbackFile = feedbackFile;
        this.zipFileName = zipFileName;
    }

    public ZipFeedbackPackager(String assessmentFolder, String submissionIdMapFile, String feedbackFile) {
        this(assessmentFolder, submissionIdMapFile, feedbackFile, "feedback.zip");
    }

    /**
     * Generates a zip file containing the feedback files for each student.
     * The zip file will be named "feedback.zip" and will be created in the assessment folder.
     */
    public void generateFeedbackZip() {

        SubmissionIdMap[] submissionIdMapArray = getSubmissionIdMap();
        BatchRequest[] batchRequests = getBatchRequest();

        Map<String,String> fileContent = new HashMap<>();


        Map<String, String> submissionIdMap = new HashMap<>();

        for (SubmissionIdMap submissionIdMapEntry : submissionIdMapArray) {

            submissionIdMap.put( submissionIdMapEntry.getSubmission_id() , submissionIdMapEntry.getCustom_id());

        }

        for (BatchRequest batchRequest : batchRequests) {

            String submissionId = batchRequest.getCustom_id();
            String response = batchRequest.getResponse().getBody().getChoices().get(0).getMessage().getContent();

            String archivo = submissionIdMap.get(submissionId) + ".md";

            fileContent.put(archivo, response);

        }

        createZipFile(fileContent);

    }

    private SubmissionIdMap[] getSubmissionIdMap(String assessmentFolder, String submissionIdMapFile) {

        Gson gson = new Gson();
        File file = new File(assessmentFolder + File.separator + submissionIdMapFile);

        try {
            String json = java.nio.file.Files.readString(file.toPath());
            return gson.fromJson(json, SubmissionIdMap[].class);

        } catch (Exception e) {
            e.printStackTrace();
            return new SubmissionIdMap[0];
        }
    }

    private SubmissionIdMap[] getSubmissionIdMap() {
        return getSubmissionIdMap(assessmentFolder, submissionIdMapFile);
    }

    private BatchRequest[] getBatchRequest(String assessmentFolder, String feedbackFile) {

        Gson gson = new Gson();
        File file = new File(assessmentFolder + File.separator + feedbackFile);

        List<BatchRequest> batchRequests = new ArrayList<>();

        try ( Scanner sc = new Scanner(new FileInputStream(file))){

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                BatchRequest batchRequest = gson.fromJson(line, BatchRequest.class);
                batchRequests.add(batchRequest);
            }
            BatchRequest[] n = new BatchRequest[batchRequests.size()];
            return batchRequests.toArray(n);

        } catch (Exception e) {
            e.printStackTrace();
            return new BatchRequest[0];
        }
    }

    private BatchRequest[] getBatchRequest() {
        return getBatchRequest(assessmentFolder, feedbackFile);
    }

    private void createZipFile(String zipFileName,Map<String, String> fileContent) {

        File zipFile = new File(assessmentFolder + File.separator + zipFileName);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream)) {

            for (Map.Entry<String, String> entry : fileContent.entrySet()) {
                String nombreArchivo = entry.getKey();
                String contenido = entry.getValue();

                ZipEntry zipEntry = new ZipEntry(nombreArchivo);
                zipOut.putNextEntry(zipEntry);
                byte[] datos = contenido.getBytes(StandardCharsets.UTF_8);
                zipOut.write(datos, 0, datos.length);
                zipOut.closeEntry();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            Files.write(zipFile.toPath(), byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error writing zip file: " + zipFile.getAbsolutePath(), e);
        }

    }

    private void createZipFile(Map<String, String> fileContent) {
        createZipFile(zipFileName, fileContent);
    }

}

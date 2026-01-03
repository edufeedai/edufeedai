/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.integration;

import com.github.edufeedai.model.openai.platform.api.OpenAIBatchProcess;
import com.github.edufeedai.model.openai.platform.api.OpenAIFileManagement;
import com.github.edufeedai.model.openai.platform.api.exceptions.OpenAIAPIException;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIBatchProcessUploadAndStartTest {

    String OpenAI_KEY = Dotenv.load().get("OPENAI_API_KEY");
    String AssessmentTestDir = Dotenv.load().get("ASSESSMENT_TEST_DIR");
    String AssessmentFile = Dotenv.load().get("ASSESSMENT_JSONL_FILE_NAME");

    
    private String uploadFileString() throws OpenAIAPIException {

        OpenAIFileManagement oaif = new OpenAIFileManagement(OpenAI_KEY);

        try {

            String fileId = oaif.uploadBatchFile(AssessmentTestDir + File.separator +  AssessmentFile);
            assertNotNull(fileId);
            return fileId;

        } catch (OpenAIAPIException e) {
            fail(e.getMessage());
        }
        return null;

    }

    private void startBatchProcess(String fileId) throws OpenAIAPIException {
        
        OpenAIBatchProcess oabp = new OpenAIBatchProcess(OpenAI_KEY);
        try {

            var job = oabp.enqueueBatchProcess(fileId);
            assertNotNull(job);
            assertNotNull(job.getId());

        } catch (OpenAIAPIException e) {
            fail(e.getMessage());
        }

    }


    @Test
    void uploadAndStartBatchProcess() {

        try {
            String fileId = uploadFileString();
            startBatchProcess(fileId);
        } catch (OpenAIAPIException e) {
            fail(e.getMessage());
        }

    }
}
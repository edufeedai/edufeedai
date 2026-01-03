/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.integration;

import com.github.edufeedai.model.openai.platform.api.OpenAIFileManagement;
import com.github.edufeedai.model.openai.platform.api.exceptions.OpenAIAPIException;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIFileUploadTest {

    String OpenAI_KEY = Dotenv.load().get("OPENAI_API_KEY");
    String AssessmentTestDir = Dotenv.load().get("ASSESSMENT_TEST_DIR");
    String AssessmentFileName = Dotenv.load().get("ASSESSMENT_JSONL_FILE_NAME");

    @Test
    void uploadBatchFileString() {

        OpenAIFileManagement oaifm = new OpenAIFileManagement(OpenAI_KEY);

        try {

            String fileId = oaifm.uploadBatchFile(AssessmentTestDir + File.separator + AssessmentFileName);
            assertNotNull(fileId);
            System.out.println("Batch file uploaded with ID: " + fileId);

        } catch (OpenAIAPIException e) {
            fail(e.getMessage());
        }

    }

    @Test
    void uploadStudentFileString() {

        OpenAIFileManagement oaifm = new OpenAIFileManagement(OpenAI_KEY);

        try {
            // Test uploading a student file (could be PDF, image, etc.)
            String fileId = oaifm.uploadStudentFile(AssessmentTestDir + File.separator + AssessmentFileName);
            assertNotNull(fileId);
            System.out.println("Student file uploaded with ID: " + fileId);

        } catch (OpenAIAPIException e) {
            fail(e.getMessage());
        }

    }
}
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
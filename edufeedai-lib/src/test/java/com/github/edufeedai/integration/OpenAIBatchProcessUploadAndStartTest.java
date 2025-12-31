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
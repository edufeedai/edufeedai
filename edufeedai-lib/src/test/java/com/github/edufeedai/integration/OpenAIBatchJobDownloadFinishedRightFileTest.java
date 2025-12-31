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

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;

class OpenAIBatchJobDownloadFinishedRightFileTest {

    String OpenAI_KEY = Dotenv.load().get("OPENAI_API_KEY");
    String AssessmentTestDir = Dotenv.load().get("ASSESSMENT_TEST_DIR");
    String AssessmentJSONLFileName = Dotenv.load().get("ASSESSMENT_JSONL_FILE_NAME");
    String OpenAIBatchJobID = Dotenv.load().get("OPENAI_BATCH_JOB_ID");

    @Test
    public void downloadFileJobFinished() throws OpenAIAPIException {

        OpenAIFileManagement oaif = new OpenAIFileManagement(OpenAI_KEY);
        
        String fileId = getCompletedFileJobFinishedName(OpenAIBatchJobID);
        String outFile = getOutputFileCompletePath(AssessmentTestDir + File.separator +AssessmentJSONLFileName);
        System.out.println("Downloading file: " + outFile);

        try {
            oaif.downloadFile(fileId, outFile);
            File downloadedFile = new File(outFile);
            assertTrue(downloadedFile.exists(), "Downloaded file does not exist");
            assertTrue(downloadedFile.length() > 0, "Downloaded file is empty");
        } catch (OpenAIAPIException e) {
            fail("Failed to download file: " + e.getMessage());
        }

    }

    private String getCompletedFileJobFinishedName(String openAIBatchJobID){

        OpenAIBatchProcess oabp = new OpenAIBatchProcess(OpenAI_KEY);

        try {
            String fileId = oabp.getBatchJob(openAIBatchJobID).getOutputFileId();
            String status = oabp.getBatchJob(openAIBatchJobID).getStatus();
            assertEquals("completed", status, "Batch job is not finished");

            assertNotNull(fileId, "File ID is null");
            return fileId;
        } catch (OpenAIAPIException e) {
            fail(e.getMessage());
        }

        return null;

    }

    private String getOutputFileCompletePath(String assessmentJSONLFile)  {
        
        Path filePath = (new File(assessmentJSONLFile)).toPath();
        String fileName = filePath.getFileName().toString();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

        String dirPath = filePath.getParent().toString();

        String outputFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "_graded." + extension;

        File outFile = new File(dirPath, outputFileName);

        return outFile.toString();


    }


   
}
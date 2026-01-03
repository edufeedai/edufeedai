/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.github.edufeedai.model.openai.platform.api.OpenAIBatchProcess;
import com.github.edufeedai.model.openai.platform.api.batches.BatchJob;

import io.github.cdimascio.dotenv.Dotenv;

public class OpenAIBatchProcessRetrieveTest {
    
    String OpenAI_KEY = Dotenv.load().get("OPENAI_API_KEY");
    String OpenAIBatchJobID = Dotenv.load().get("OPENAI_BATCH_JOB_ID");

    @Test
    void retrieveBatchProcess() {
        
        try {
            OpenAIBatchProcess oabp = new OpenAIBatchProcess(OpenAI_KEY);
            BatchJob job = oabp.getBatchJob(OpenAIBatchJobID);
            assertNotNull(job);
            assertEquals(job.getId(), OpenAIBatchJobID);
            assertNotNull(job.getStatus());
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

}

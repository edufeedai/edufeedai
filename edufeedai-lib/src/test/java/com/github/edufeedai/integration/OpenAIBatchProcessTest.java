/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.integration;

import com.github.edufeedai.model.openai.platform.api.OpenAIBatchProcess;
import com.github.edufeedai.model.openai.platform.api.batches.BatchJob;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIBatchProcessTest {

    String OpenAI_KEY = Dotenv.load().get("OPENAI_API_KEY");
    String OpenAIBatchFileID = Dotenv.load().get("OPENAI_BATCH_FILE_ID");

    @Test
    void enqueueBatchProcess() {

        OpenAIBatchProcess oabp = new OpenAIBatchProcess(OpenAI_KEY);

        try {

            BatchJob job = oabp.enqueueBatchProcess(OpenAIBatchFileID);
            assertNotNull(job);
            assertNotNull(job.getId());

        } catch (Exception e) {
            fail(e);
        }
    }
}
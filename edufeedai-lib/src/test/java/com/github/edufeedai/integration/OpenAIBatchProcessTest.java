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
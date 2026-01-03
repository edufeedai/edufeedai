/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ZipFeedbackPackagerTest {

    String assessmentDirectory = Dotenv.load().get("ASSESSMENT_TEST_DIR");
    String assessmentIdMapFile = Dotenv.load().get("ASSESSMENT_ID_MAP_FILE_NAME");
    String assessmentResponsesFile = Dotenv.load().get("ASSESSMENT_RESPONSES_FILE_NAME");

    @Test
    void generateFeedbackZip() {

        assertNotNull(assessmentDirectory, "ASSESSMENT_TEST_DIR environment variable is not set");

        ZipFeedbackPackager packager = new ZipFeedbackPackager(
                assessmentDirectory,
                assessmentIdMapFile,
                assessmentResponsesFile
        );


        try {
            packager.generateFeedbackZip();
        } catch (Exception e) {
            fail("Failed to generate feedback zip: " + e.getMessage());
        }

        // Check if the zip file was created
        File dir = new File(assessmentDirectory);
        File[] zipFiles = dir.listFiles((d, name) -> name.endsWith(".zip"));
        assertNotNull(zipFiles, "No zip files found in the assessment directory");
        assertTrue(zipFiles.length > 0, "No zip files were generated");
    }
}
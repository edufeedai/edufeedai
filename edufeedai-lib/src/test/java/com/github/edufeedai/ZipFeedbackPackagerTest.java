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
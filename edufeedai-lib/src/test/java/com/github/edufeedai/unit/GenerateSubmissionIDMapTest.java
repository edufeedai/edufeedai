/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.unit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.edufeedai.GenerateSubmissionIDMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.edufeedai.annotations.FileIOTest;
import com.github.edufeedai.annotations.IntegrationTest;
import com.github.edufeedai.annotations.PrivacyTest;
import com.github.edufeedai.annotations.UtilityTest;
import com.github.edufeedai.model.DigestSHA1;
import com.github.edufeedai.model.SubmissionIdMap;

import io.github.cdimascio.dotenv.Dotenv;


class GenerateSubmissionIDMapTest {

    String assessmentFolder = Dotenv.load().get("ASSESSMENT_TEST_DIR");
    String assessmmentIDMapFile = Dotenv.load().get("ASSESSMENT_ID_MAP_FILE_NAME");

    @Test
    @IntegrationTest
    @UtilityTest
    @FileIOTest
    @PrivacyTest
    @DisplayName("Genera y guarda el mapa de IDs de envío")
    void saveSubmissionIDMaps() {
        
        GenerateSubmissionIDMap generateSubmissionIDMap = new GenerateSubmissionIDMap(assessmentFolder,new DigestSHA1());
        SubmissionIdMap[] submissionIdMaps = generateSubmissionIDMap.generateSubmissionIDMaps();

        assertNotNull(submissionIdMaps, "El array de SubmissionIdMap no debe ser nulo");
        assertTrue(submissionIdMaps.length > 0, "El array de SubmissionIdMap debe contener al menos un elemento");

        String filename = generateSubmissionIDMap.saveSubmissionIDMaps(submissionIdMaps, assessmmentIDMapFile);
        assertNotNull(filename, "El nombre del archivo generado no debe ser nulo");

    }
}
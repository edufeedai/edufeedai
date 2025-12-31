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
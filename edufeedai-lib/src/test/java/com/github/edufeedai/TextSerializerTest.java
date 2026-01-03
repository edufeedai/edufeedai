/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.github.edufeedai.annotations.IntegrationTest;
import com.github.edufeedai.model.AssessmentGradingConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import io.github.cdimascio.dotenv.Dotenv;

@IntegrationTest
class TextSerializerTest {

    String assessmentDirectory = Dotenv.load().get("ASSESSMENT_TEST_DIR");

    @ParameterizedTest
    @ArgumentsSource(AssessmentGradingArgumentsProvider.class)
    void packageFiles(AssessmentGradingConfig config) {

        
        TextSerializer serializer = new TextSerializer(assessmentDirectory);

        serializer.deleteAllStudentJsonFiles();

        try {
            serializer.packageFiles(config.getInstruction());
        } catch (Exception e) {
            fail(e);
        }

        try {
            serializer.generateJsonl();
        } catch (Exception e) {
            fail(e);
        }

        // Buscar el archivo .jsonl generado en assessmentDirectory
        File dir = new File(assessmentDirectory);
        File[] jsonlFiles = dir.listFiles((d, name) -> name.endsWith(".jsonl"));
        Assertions.assertNotNull(jsonlFiles, "No se encontraron archivos .jsonl en el assessmentDirectory");
        Assertions.assertTrue(jsonlFiles.length > 0, "No se generó ningún archivo .jsonl");

        Gson gson = new Gson();
        for (File jsonlFile : jsonlFiles) {
            try (Scanner reader = new Scanner(new FileReader(jsonlFile))) {
                String line;
                int lineNumber = 1;
                while ((reader.hasNextLine())) {
                    line = reader.nextLine();
                    try {
                        JsonElement element = JsonParser.parseString(line);
                        Assertions.assertTrue(element.isJsonObject(),
                            "La línea " + lineNumber + " del archivo '" + jsonlFile.getName() + "' no es un objeto JSON: " + line);
                    } catch (JsonSyntaxException e) {
                        Assertions.fail("La línea " + lineNumber + " del archivo '" + jsonlFile.getName() + "' no es JSON válido: " + line);
                    }
                    lineNumber++;
                }
            } catch (Exception e) {
                fail("Error al leer el archivo '" + jsonlFile.getName() + "': " + e.getMessage());
            }
        }
        

    }
}

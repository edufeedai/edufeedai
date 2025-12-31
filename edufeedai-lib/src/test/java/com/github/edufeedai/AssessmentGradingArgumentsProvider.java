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

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import com.github.edufeedai.model.AssessmentGradingConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.github.cdimascio.dotenv.Dotenv;

public class AssessmentGradingArgumentsProvider  implements ArgumentsProvider {

    private static final Gson gson = new Gson();

    private final String assessmentDirectory = Dotenv.load().get("ASSESSMENT_TEST_DIR");
    private final String assessmentGradingConfigFileName = Dotenv.load().get("ASSESSMENT_GRADING_CONFIG_FILE_NAME");

    private final String assessmentGradingConfigFilePath = assessmentDirectory + "/" + assessmentGradingConfigFileName;

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
        return Stream.of(assessmentGradingConfigFilePath)
                .map(this::loadJson)
                .map(Arguments::of);
    }
    
    private AssessmentGradingConfig loadJson(String filename){
            try (FileReader reader = new FileReader(filename)) {
                Type type = new TypeToken<AssessmentGradingConfig>(){}.getType();
                return gson.fromJson(reader, type);
            } catch (IOException e) {
                throw new RuntimeException("Unable to load JSON file: " + filename, e);
            }
    }
}


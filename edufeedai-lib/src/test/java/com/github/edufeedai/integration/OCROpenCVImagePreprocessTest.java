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

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import com.github.edufeedai.model.ocrlib.OCROpenCVImagePreprocess;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;

@Disabled
class OCROpenCVImagePreprocessTest {

    String PNG_ORIGIN_IMAGE_OCR_TEST = Dotenv.load().get("PNG_ORIGIN_IMAGE_OCR_TEST");
    String PNG_DESTINATION_IMAGE_OCR_TEST = Dotenv.load().get("PNG_DESTINATION_IMAGE_OCR_TEST");

    @Test
    void binarize() {

        try {

            OCROpenCVImagePreprocess.Binarize(PNG_ORIGIN_IMAGE_OCR_TEST, PNG_DESTINATION_IMAGE_OCR_TEST);
        } catch (Exception e) {
            fail(e);
        }

    }
}
/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
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
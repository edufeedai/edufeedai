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

import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.edufeedai.model.PDFExtractTextAndImagesOrdered;
import com.github.edufeedai.model.exceptions.PDFExtractTextAndImageException;
import com.github.edufeedai.model.ocrlib.OCRProcessor;
import com.github.edufeedai.model.ocrlib.OCRProcessorTesseract;

import io.github.cdimascio.dotenv.Dotenv;

@Disabled 
class ExtractTextAndImagesTest {

    String assessmenetPDFFile = Dotenv.load().get("ASSESSMENT_PDF_FILE");

    @Test
    @DisplayName("Extraer imágenes y texto de un archivo PDF con Tesseract")
    void extractImagesAndTextFromPDFFileTesseract() {

        try {

            OCRProcessor ocrProcessor = new OCRProcessorTesseract();

            PDFExtractTextAndImagesOrdered extractTextAndImagesOrdered = new PDFExtractTextAndImagesOrdered(ocrProcessor);
            extractTextAndImagesOrdered.extractImagesAndTextFromPDFFile(assessmenetPDFFile);

        } catch (PDFExtractTextAndImageException e) {
            fail(e);
        }

    }
}
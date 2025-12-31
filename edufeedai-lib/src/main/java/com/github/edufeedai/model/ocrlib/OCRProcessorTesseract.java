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

package com.github.edufeedai.model.ocrlib;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

import java.io.File;

public class OCRProcessorTesseract implements OCRProcessor {

    final private ITesseract tesseract;

    public OCRProcessorTesseract() {

        this.tesseract = new Tesseract();
        tesseract.setDatapath("/usr/share/tesseract-ocr/5/tessdata");
        tesseract.setLanguage("eng"); // Cambia según el idioma que desees usar
        tesseract.setVariable("preserve_interword_spaces", "1");
        tesseract.setPageSegMode(6);
        tesseract.setVariable("user_defined_dpi", "300"); //PDFBox no proporciona DPI, por lo que es necesario definirlo
        //tesseract.setVariable("tessedit_char_whitelist", "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.,:/-_@()[]{}#$%&*=+><|!?~ \n");

    }

    @Override
    public String performOCR(File imageFile) throws OCRProcessorException {

        try {

            OCROpenCVImagePreprocess.Binarize(imageFile.getAbsolutePath());
            return tesseract.doOCR(imageFile);

        } catch (Exception e) {

            throw new OCRProcessorException(e);

        }
    }

}

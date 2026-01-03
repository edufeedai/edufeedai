/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
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

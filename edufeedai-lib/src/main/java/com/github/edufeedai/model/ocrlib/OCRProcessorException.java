/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model.ocrlib;

public class OCRProcessorException extends Exception {

    public OCRProcessorException(Exception e) {
        super(e);
    }

    public OCRProcessorException(String message) {
        super(message);
    }

    public OCRProcessorException(String message, Exception e) {
        super(message, e);
    }

    public OCRProcessorException() {
        super();
    }

}

/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model.ocrlib;

import java.io.File;

public interface OCRProcessor {

    String performOCR(File imageFile) throws OCRProcessorException;

}

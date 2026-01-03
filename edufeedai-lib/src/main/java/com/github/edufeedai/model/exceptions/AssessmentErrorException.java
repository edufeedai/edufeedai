/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model.exceptions;

public class AssessmentErrorException extends Exception {

    public AssessmentErrorException(){
        super();
    }

    public AssessmentErrorException(Exception e) {
        super(e);
    }
}

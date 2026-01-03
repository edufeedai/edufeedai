/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model.openai.platform.api.interfaces.exceptions;

public class APIException extends Exception {
    
    public APIException() {
        super();
    }

    public APIException(String s) {
        super(s);
    }

    public APIException(Exception e) {
        super(e);
    }

    public APIException(String s, Exception e) {
        super(s, e);
    }
}

/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model.exceptions;

public class PDFExtractTextAndImageException extends Exception{

    public PDFExtractTextAndImageException(){
        super();
    }

    public PDFExtractTextAndImageException(String message){
        super(message);
    }

    public PDFExtractTextAndImageException(Exception e){
        super(e);
    }

}

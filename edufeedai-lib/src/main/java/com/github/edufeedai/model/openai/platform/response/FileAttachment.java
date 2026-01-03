/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model.openai.platform.response;

import java.util.List;

/**
 * Represents a file attachment in an OpenAI API message.
 * Used to reference files uploaded to OpenAI's file storage.
 */
public class FileAttachment {

    private String file_id;
    private List<String> tools;

    public FileAttachment() {
    }

    public FileAttachment(String fileId, List<String> tools) {
        this.file_id = fileId;
        this.tools = tools;
    }

    public String getFile_id() {
        return file_id;
    }

    public void setFile_id(String file_id) {
        this.file_id = file_id;
    }

    public List<String> getTools() {
        return tools;
    }

    public void setTools(List<String> tools) {
        this.tools = tools;
    }
}

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

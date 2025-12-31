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

package com.github.edufeedai.model;

/**
 * Representa un archivo adjunto a una entrega de estudiante.
 */
public class SubmissionFile {

    private Integer id;
    private Integer submissionId;
    private String filePath;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String openaiFileId;
    private Boolean isTextFile;
    private String contentExtracted;
    private Long createdAt;

    public SubmissionFile() {
    }

    public SubmissionFile(Integer submissionId, String filePath, String fileName, Long createdAt) {
        this.submissionId = submissionId;
        this.filePath = filePath;
        this.fileName = fileName;
        this.createdAt = createdAt;
        this.isTextFile = false;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Integer submissionId) {
        this.submissionId = submissionId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getOpenaiFileId() {
        return openaiFileId;
    }

    public void setOpenaiFileId(String openaiFileId) {
        this.openaiFileId = openaiFileId;
    }

    public Boolean getIsTextFile() {
        return isTextFile;
    }

    public void setIsTextFile(Boolean isTextFile) {
        this.isTextFile = isTextFile;
    }

    public String getContentExtracted() {
        return contentExtracted;
    }

    public void setContentExtracted(String contentExtracted) {
        this.contentExtracted = contentExtracted;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}

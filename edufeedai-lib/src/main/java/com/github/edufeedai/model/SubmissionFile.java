/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
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

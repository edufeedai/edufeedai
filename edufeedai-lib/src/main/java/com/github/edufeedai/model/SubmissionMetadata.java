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

import java.util.List;

/**
 * Metadatos de una entrega almacenados en .submission-metadata.json
 */
public class SubmissionMetadata {

    private Integer submissionId;
    private String studentName;
    private Integer submissionNumber;
    private Long submittedAt;
    private List<FileMetadata> files;

    public SubmissionMetadata() {
    }

    public SubmissionMetadata(Integer submissionId, String studentName, Integer submissionNumber, Long submittedAt) {
        this.submissionId = submissionId;
        this.studentName = studentName;
        this.submissionNumber = submissionNumber;
        this.submittedAt = submittedAt;
    }

    public Integer getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Integer submissionId) {
        this.submissionId = submissionId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Integer getSubmissionNumber() {
        return submissionNumber;
    }

    public void setSubmissionNumber(Integer submissionNumber) {
        this.submissionNumber = submissionNumber;
    }

    public Long getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Long submittedAt) {
        this.submittedAt = submittedAt;
    }

    public List<FileMetadata> getFiles() {
        return files;
    }

    public void setFiles(List<FileMetadata> files) {
        this.files = files;
    }

    /**
     * Clase interna para metadatos de archivo individual
     */
    public static class FileMetadata {
        private String fileName;
        private String fileType;
        private Long fileSize;
        private Boolean isTextFile;

        public FileMetadata() {
        }

        public FileMetadata(String fileName, String fileType, Long fileSize, Boolean isTextFile) {
            this.fileName = fileName;
            this.fileType = fileType;
            this.fileSize = fileSize;
            this.isTextFile = isTextFile;
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

        public Boolean getIsTextFile() {
            return isTextFile;
        }

        public void setIsTextFile(Boolean isTextFile) {
            this.isTextFile = isTextFile;
        }
    }
}

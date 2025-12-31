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
 * Representa una entrega de estudiante para una tarea.
 */
public class Submission {

    private Integer id;
    private Integer taskId;
    private String studentName;
    private Integer submissionNumber;
    private String submissionId;
    private String batchId;
    private String status;
    private String feedback;
    private Double grade;
    private Long submittedAt;
    private Long processedAt;
    private Long createdAt;

    public Submission() {
    }

    public Submission(Integer taskId, String studentName, Integer submissionNumber, String submissionId, Long submittedAt, Long createdAt) {
        this.taskId = taskId;
        this.studentName = studentName;
        this.submissionNumber = submissionNumber;
        this.submissionId = submissionId;
        this.submittedAt = submittedAt;
        this.createdAt = createdAt;
        this.status = "pending";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
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

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public Long getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Long submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Long getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Long processedAt) {
        this.processedAt = processedAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}

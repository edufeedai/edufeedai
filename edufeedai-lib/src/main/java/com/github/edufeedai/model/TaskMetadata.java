/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model;

/**
 * Metadatos de una tarea almacenados en .task-metadata.json
 */
public class TaskMetadata {

    private Integer taskId;
    private String taskName;
    private Integer gradingConfigId;
    private Long createdAt;

    public TaskMetadata() {
    }

    public TaskMetadata(Integer taskId, String taskName, Integer gradingConfigId, Long createdAt) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.gradingConfigId = gradingConfigId;
        this.createdAt = createdAt;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Integer getGradingConfigId() {
        return gradingConfigId;
    }

    public void setGradingConfigId(Integer gradingConfigId) {
        this.gradingConfigId = gradingConfigId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}

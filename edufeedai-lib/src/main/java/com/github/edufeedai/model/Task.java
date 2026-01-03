/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model;

/**
 * Representa una tarea o actividad de evaluación.
 */
public class Task {

    private Integer id;
    private String name;
    private Integer gradingConfigId;
    private Long createdAt;
    private Long updatedAt;

    public Task() {
    }

    public Task(String name, Integer gradingConfigId, Long createdAt) {
        this.name = name;
        this.gradingConfigId = gradingConfigId;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}

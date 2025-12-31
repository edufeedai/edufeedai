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

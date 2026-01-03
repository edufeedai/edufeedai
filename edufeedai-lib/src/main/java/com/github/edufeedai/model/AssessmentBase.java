/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model;

class AssessmentBase {

    public String id;

    public String gradingCriteria;

    public String taskSubmitted;

    public String assessmentFeedback;

    public AssessmentBase() {

    }

    public AssessmentBase(String gradingCriteria, String taskSubmitted) {
        this();
        this.gradingCriteria = gradingCriteria;
        this.taskSubmitted = taskSubmitted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGradingCriteria() {
        return gradingCriteria;
    }

    public void setGradingCriteria(String gradingCriteria) {
        this.gradingCriteria = gradingCriteria;
    }

    public String getTaskSubmitted() {
        return taskSubmitted;
    }

    public void setTaskSubmitted(String taskSubmitted) {
        this.taskSubmitted = taskSubmitted;
    }

    public String getAssessmentFeedback() {
        return assessmentFeedback;
    }

    public void setAssessmentFeedback(String assessmentFeedback) {
        this.assessmentFeedback = assessmentFeedback;
    }
}

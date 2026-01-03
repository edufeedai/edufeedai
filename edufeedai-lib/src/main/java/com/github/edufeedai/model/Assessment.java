/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model;

public class Assessment {

    private final String id;

    private final String gradingCriteria;

    private final String taskSubmitted;

    private String assesmentFeedback;

    protected Assessment(AssessmentBase assessmentBase) {

        this(assessmentBase.getId(), assessmentBase.getGradingCriteria(), assessmentBase.getTaskSubmitted());

    }

    protected Assessment(String id, String gradingCriteria, String taskSubmitted) {
        this.id = id;
        this.gradingCriteria = gradingCriteria;
        this.taskSubmitted = taskSubmitted;
    }

    public String getId() {
        return id;
    }

    public String getGradingCriteria() {
        return gradingCriteria;
    }

    public String getTaskSubmitted() {
        return taskSubmitted;
    }

    public String getAssesmentFeedback() {
        return assesmentFeedback;
    }

    protected void setAssesmentFeedback(String assesmentFeedback) {
        this.assesmentFeedback = assesmentFeedback;
    }


}

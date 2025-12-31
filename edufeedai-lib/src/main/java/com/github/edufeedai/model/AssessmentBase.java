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

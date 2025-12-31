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

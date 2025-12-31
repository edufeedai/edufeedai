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

package com.github.edufeedai.model.openai.platform.api.batches;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class BatchJob {

    @SerializedName("id")
    private String id;

    @SerializedName("object")
    private String object;

    @SerializedName("endpoint")
    private String endpoint;

    @SerializedName("errors")
    private Object errors; // Puede ser null o contener información de error

    @SerializedName("input_file_id")
    private String inputFileId;

    @SerializedName("completion_window")
    private String completionWindow;

    @SerializedName("status")
    private String status;

    @SerializedName("output_file_id")
    private String outputFileId;

    @SerializedName("error_file_id")
    private String errorFileId;

    @SerializedName("created_at")
    private Long createdAt;

    @SerializedName("in_progress_at")
    private Long inProgressAt;

    @SerializedName("expires_at")
    private Long expiresAt;

    @SerializedName("finalizing_at")
    private Long finalizingAt;

    @SerializedName("completed_at")
    private Long completedAt;

    @SerializedName("failed_at")
    private Long failedAt;

    @SerializedName("expired_at")
    private Long expiredAt;

    @SerializedName("cancelling_at")
    private Long cancellingAt;

    @SerializedName("cancelled_at")
    private Long cancelledAt;

    @SerializedName("request_counts")
    private RequestCounts requestCounts;

    @SerializedName("metadata")
    private Map<String, Object> metadata; // Este campo puede contener cualquier valor arbitrario o estar vacío

    // Getters y Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Object getErrors() {
        return errors;
    }

    public void setErrors(Object errors) {
        this.errors = errors;
    }

    public String getInputFileId() {
        return inputFileId;
    }

    public void setInputFileId(String inputFileId) {
        this.inputFileId = inputFileId;
    }

    public String getCompletionWindow() {
        return completionWindow;
    }

    public void setCompletionWindow(String completionWindow) {
        this.completionWindow = completionWindow;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOutputFileId() {
        return outputFileId;
    }

    public void setOutputFileId(String outputFileId) {
        this.outputFileId = outputFileId;
    }

    public String getErrorFileId() {
        return errorFileId;
    }

    public void setErrorFileId(String errorFileId) {
        this.errorFileId = errorFileId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getInProgressAt() {
        return inProgressAt;
    }

    public void setInProgressAt(Long inProgressAt) {
        this.inProgressAt = inProgressAt;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getFinalizingAt() {
        return finalizingAt;
    }

    public void setFinalizingAt(Long finalizingAt) {
        this.finalizingAt = finalizingAt;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }

    public Long getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Long failedAt) {
        this.failedAt = failedAt;
    }

    public Long getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Long expiredAt) {
        this.expiredAt = expiredAt;
    }

    public Long getCancellingAt() {
        return cancellingAt;
    }

    public void setCancellingAt(Long cancellingAt) {
        this.cancellingAt = cancellingAt;
    }

    public Long getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Long cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public RequestCounts getRequestCounts() {
        return requestCounts;
    }

    public void setRequestCounts(RequestCounts requestCounts) {
        this.requestCounts = requestCounts;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    // Clase interna para RequestCounts
    public static class RequestCounts {

        @SerializedName("total")
        private int total;

        @SerializedName("completed")
        private int completed;

        @SerializedName("failed")
        private int failed;

        // Getters y Setters

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getCompleted() {
            return completed;
        }

        public void setCompleted(int completed) {
            this.completed = completed;
        }

        public int getFailed() {
            return failed;
        }

        public void setFailed(int failed) {
            this.failed = failed;
        }
    }


}

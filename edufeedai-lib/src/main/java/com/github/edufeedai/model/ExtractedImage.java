/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model;

/**
 * Representa una imagen extraída de un PDF.
 */
public class ExtractedImage {
    private final String relativePath;
    private final String mimeType;
    private final int pageNumber;
    private final int imageIndex;
    private final long fileSize;
    private final int width;
    private final int height;
    private boolean isDuplicate;

    public ExtractedImage(String relativePath, String mimeType, int pageNumber,
                         int imageIndex, long fileSize, int width, int height) {
        this.relativePath = relativePath;
        this.mimeType = mimeType;
        this.pageNumber = pageNumber;
        this.imageIndex = imageIndex;
        this.fileSize = fileSize;
        this.width = width;
        this.height = height;
        this.isDuplicate = false;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getImageIndex() {
        return imageIndex;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isDuplicate() {
        return isDuplicate;
    }

    public void setDuplicate(boolean duplicate) {
        isDuplicate = duplicate;
    }

    @Override
    public String toString() {
        return String.format("ExtractedImage{path='%s', mime='%s', page=%d, idx=%d, size=%d bytes, %dx%d, duplicate=%b}",
                relativePath, mimeType, pageNumber, imageIndex, fileSize, width, height, isDuplicate);
    }
}

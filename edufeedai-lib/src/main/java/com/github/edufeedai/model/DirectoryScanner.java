/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Escanea la estructura de directorios del workspace.
 * Detecta tareas, entregas y archivos.
 */
public class DirectoryScanner {

    private final WorkspaceManager workspaceManager;

    public DirectoryScanner(WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    /**
     * Escanea el workspace y retorna todas las tareas encontradas.
     */
    public List<TaskDirectory> scanWorkspace() throws IOException {
        List<TaskDirectory> tasks = new ArrayList<>();
        Path workspaceRoot = workspaceManager.getWorkspaceRoot();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(workspaceRoot)) {
            for (Path entry : stream) {
                if (workspaceManager.isTaskDirectory(entry)) {
                    TaskMetadata metadata = workspaceManager.readTaskMetadata(entry);
                    List<SubmissionDirectory> submissions = scanTask(entry);
                    tasks.add(new TaskDirectory(entry, metadata, submissions));
                }
            }
        }

        return tasks;
    }

    /**
     * Escanea un directorio de tarea y retorna todas las entregas encontradas.
     */
    public List<SubmissionDirectory> scanTask(Path taskDir) throws IOException {
        List<SubmissionDirectory> submissions = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(taskDir)) {
            for (Path entry : stream) {
                if (workspaceManager.isSubmissionDirectory(entry)) {
                    SubmissionMetadata metadata = workspaceManager.readSubmissionMetadata(entry);
                    List<File> files = scanSubmission(entry);
                    submissions.add(new SubmissionDirectory(entry, metadata, files));
                }
            }
        }

        return submissions;
    }

    /**
     * Escanea un directorio de entrega y retorna todos los archivos (excluyendo metadatos).
     */
    public List<File> scanSubmission(Path submissionDir) throws IOException {
        List<File> files = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(submissionDir)) {
            for (Path entry : stream) {
                File file = entry.toFile();
                // Excluir archivos de metadatos y directorios
                if (file.isFile() && !file.getName().startsWith(".")) {
                    files.add(file);
                }
            }
        }

        return files;
    }

    /**
     * Clase contenedora para un directorio de tarea escaneado.
     */
    public static class TaskDirectory {
        private final Path path;
        private final TaskMetadata metadata;
        private final List<SubmissionDirectory> submissions;

        public TaskDirectory(Path path, TaskMetadata metadata, List<SubmissionDirectory> submissions) {
            this.path = path;
            this.metadata = metadata;
            this.submissions = submissions;
        }

        public Path getPath() {
            return path;
        }

        public TaskMetadata getMetadata() {
            return metadata;
        }

        public List<SubmissionDirectory> getSubmissions() {
            return submissions;
        }
    }

    /**
     * Clase contenedora para un directorio de entrega escaneado.
     */
    public static class SubmissionDirectory {
        private final Path path;
        private final SubmissionMetadata metadata;
        private final List<File> files;

        public SubmissionDirectory(Path path, SubmissionMetadata metadata, List<File> files) {
            this.path = path;
            this.metadata = metadata;
            this.files = files;
        }

        public Path getPath() {
            return path;
        }

        public SubmissionMetadata getMetadata() {
            return metadata;
        }

        public List<File> getFiles() {
            return files;
        }
    }
}

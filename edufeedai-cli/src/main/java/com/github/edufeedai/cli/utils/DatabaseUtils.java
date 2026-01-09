/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.cli.utils;

import com.github.edufeedai.cli.config.AppConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utilidades para gestión de base de datos SQLite.
 */
public class DatabaseUtils {

    /**
     * Crea la conexión a la base de datos.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + AppConfig.getDatabasePath());
    }

    /**
     * Crea la base de datos y todas las tablas necesarias.
     */
    public static void createDatabase(Path workDir) throws SQLException {
        Path configPath = workDir.resolve(AppConfig.getConfigFolder());
        Path dbPath = configPath.resolve("edufeedai.db");

        try {
            // Crear el directorio .edufeedai si no existe
            Files.createDirectories(configPath);
        } catch (IOException e) {
            throw new SQLException("No se pudo crear el directorio " + configPath, e);
        }

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toString())) {
            Statement stmt = conn.createStatement();

            // Habilitar foreign keys en SQLite
            stmt.executeUpdate("PRAGMA foreign_keys = ON");

            // Tabla: grading_config (configuraciones de corrección)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS grading_config (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "message_role_system TEXT, " +
                "context TEXT, " +
                "activity_statement TEXT, " +
                "rubric TEXT, " +
                "generated_instructions TEXT, " +
                "created_at INTEGER)");

            // Tabla: tasks (tareas/actividades)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "moodle_task_id TEXT, " +
                "grading_config_id INTEGER, " +
                "created_at INTEGER NOT NULL, " +
                "updated_at INTEGER, " +
                "last_check_timestamp INTEGER, " +
                "cached_batch_status TEXT, " +
                "FOREIGN KEY (grading_config_id) REFERENCES grading_config(id))");

            // Tabla: submissions (entregas de estudiantes)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS submissions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "task_id INTEGER NOT NULL, " +
                "student_name TEXT NOT NULL, " +
                "submission_number INTEGER DEFAULT 1, " +
                "submission_id TEXT UNIQUE, " +
                "batch_id TEXT, " +
                "status TEXT DEFAULT 'pending', " +
                "feedback TEXT, " +
                "grade REAL, " +
                "submitted_at INTEGER NOT NULL, " +
                "processed_at INTEGER, " +
                "created_at INTEGER NOT NULL, " +
                "updated_at INTEGER, " +
                "FOREIGN KEY (task_id) REFERENCES tasks(id), " +
                "UNIQUE(task_id, student_name, submission_number))");

            // Tabla: submission_files (archivos de cada entrega)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS submission_files (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "submission_id INTEGER NOT NULL, " +
                "file_path TEXT NOT NULL, " +
                "file_name TEXT NOT NULL, " +
                "file_type TEXT, " +
                "file_size INTEGER, " +
                "openai_file_id TEXT, " +
                "is_text_file INTEGER DEFAULT 0, " +
                "content_extracted TEXT, " +
                "created_at INTEGER NOT NULL, " +
                "updated_at INTEGER, " +
                "FOREIGN KEY (submission_id) REFERENCES submissions(id) ON DELETE CASCADE)");

            // Tabla: submission_images (imágenes extraídas de PDFs)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS submission_images (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "submission_id INTEGER NOT NULL, " +
                "relative_path TEXT NOT NULL, " +
                "mime_type TEXT NOT NULL, " +
                "is_duplicate INTEGER DEFAULT 0, " +
                "page_number INTEGER NOT NULL, " +
                "image_index INTEGER NOT NULL, " +
                "file_size INTEGER, " +
                "width INTEGER, " +
                "height INTEGER, " +
                "dhash TEXT, " +
                "phash TEXT, " +
                "openai_file_id TEXT, " +
                "vision_description TEXT, " +
                "created_at INTEGER NOT NULL, " +
                "updated_at INTEGER, " +
                "FOREIGN KEY (submission_id) REFERENCES submissions(id) ON DELETE CASCADE, " +
                "UNIQUE(submission_id, relative_path))");

            // Índices para submission_images
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_submission_images_submission " +
                "ON submission_images(submission_id)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_submission_images_duplicate " +
                "ON submission_images(is_duplicate)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_submission_images_openai_file " +
                "ON submission_images(openai_file_id)");
        }
    }

    /**
     * Mapea el estado de batch de OpenAI a estado de submission.
     */
    public static String mapBatchStatusToSubmissionStatus(String batchStatus) {
        switch (batchStatus) {
            case "completed":
                return "completed";
            case "failed":
            case "expired":
            case "cancelled":
                return "failed";
            case "in_progress":
            case "finalizing":
                return "processing";
            default:
                return "pending";
        }
    }

    /**
     * Verifica si se debe actualizar el estado del batch desde OpenAI.
     */
    public static boolean shouldUpdateBatchStatus(Connection conn, int taskId) throws SQLException {
        long now = System.currentTimeMillis() / 1000;
        long interval = AppConfig.getBatchStatusCheckInterval();

        PreparedStatement stmt = conn.prepareStatement(
            "SELECT last_check_timestamp FROM tasks WHERE id = ?"
        );
        stmt.setInt(1, taskId);
        var rs = stmt.executeQuery();

        if (rs.next()) {
            Long lastCheck = rs.getLong("last_check_timestamp");
            if (lastCheck == null || lastCheck == 0) {
                return true;
            }
            return (now - lastCheck) >= interval;
        }

        return true;
    }

    /**
     * Actualiza el caché de estado del batch en la base de datos.
     */
    public static void updateCachedBatchStatus(Connection conn, int taskId, String status) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
            "UPDATE tasks SET last_check_timestamp = ?, cached_batch_status = ? WHERE id = ?"
        );
        stmt.setLong(1, System.currentTimeMillis() / 1000);
        stmt.setString(2, status);
        stmt.setInt(3, taskId);
        stmt.executeUpdate();
    }

    /**
     * Obtiene el estado del batch cacheado.
     */
    public static String getCachedBatchStatus(Connection conn, int taskId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT cached_batch_status FROM tasks WHERE id = ?"
        );
        stmt.setInt(1, taskId);
        var rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getString("cached_batch_status");
        }

        return null;
    }

    /**
     * Inserta una imagen extraída en la base de datos.
     * @param conn Conexión a la base de datos
     * @param submissionId ID de la entrega
     * @param relativePath Ruta relativa de la imagen
     * @param mimeType MIME type de la imagen
     * @param pageNumber Número de página del PDF
     * @param imageIndex Índice de la imagen en la página
     * @param fileSize Tamaño del archivo en bytes
     * @param width Ancho de la imagen
     * @param height Alto de la imagen
     * @param dHash Difference hash (perceptual)
     * @param pHash Perceptive hash
     * @return ID de la imagen insertada
     */
    public static long insertSubmissionImage(Connection conn, int submissionId, String relativePath,
                                            String mimeType, int pageNumber, int imageIndex,
                                            long fileSize, int width, int height,
                                            String dHash, String pHash) throws SQLException {
        long now = System.currentTimeMillis() / 1000;

        PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO submission_images (submission_id, relative_path, mime_type, " +
            "page_number, image_index, file_size, width, height, dhash, phash, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS
        );

        stmt.setInt(1, submissionId);
        stmt.setString(2, relativePath);
        stmt.setString(3, mimeType);
        stmt.setInt(4, pageNumber);
        stmt.setInt(5, imageIndex);
        stmt.setLong(6, fileSize);
        stmt.setInt(7, width);
        stmt.setInt(8, height);
        stmt.setString(9, dHash);
        stmt.setString(10, pHash);
        stmt.setLong(11, now);
        stmt.setLong(12, now);

        stmt.executeUpdate();

        var rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            return rs.getLong(1);
        }

        throw new SQLException("No se pudo obtener el ID de la imagen insertada");
    }

    /**
     * Actualiza el openai_file_id de una imagen.
     */
    public static void updateImageOpenAIFileId(Connection conn, long imageId, String openaiFileId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
            "UPDATE submission_images SET openai_file_id = ?, updated_at = ? WHERE id = ?"
        );
        stmt.setString(1, openaiFileId);
        stmt.setLong(2, System.currentTimeMillis() / 1000);
        stmt.setLong(3, imageId);
        stmt.executeUpdate();
    }

    /**
     * Actualiza la descripción de Vision API de una imagen.
     */
    public static void updateImageVisionDescription(Connection conn, long imageId, String description) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
            "UPDATE submission_images SET vision_description = ?, updated_at = ? WHERE id = ?"
        );
        stmt.setString(1, description);
        stmt.setLong(2, System.currentTimeMillis() / 1000);
        stmt.setLong(3, imageId);
        stmt.executeUpdate();
    }

    /**
     * Marca una imagen como duplicada.
     */
    public static void markImageAsDuplicate(Connection conn, long imageId, boolean isDuplicate) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
            "UPDATE submission_images SET is_duplicate = ?, updated_at = ? WHERE id = ?"
        );
        stmt.setInt(1, isDuplicate ? 1 : 0);
        stmt.setLong(2, System.currentTimeMillis() / 1000);
        stmt.setLong(3, imageId);
        stmt.executeUpdate();
    }
}

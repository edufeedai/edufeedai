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
}

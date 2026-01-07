/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.cli.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Gestiona la configuración de la aplicación desde .env y valores por defecto.
 */
public class AppConfig {

    private static final String CONFIG_FOLDER = ".edufeedai";
    private static final Dotenv dotenv = loadDotenv();

    /**
     * Carga el archivo .env si existe, si no, devuelve una instancia que lee del sistema.
     */
    private static Dotenv loadDotenv() {
        try {
            return Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
        } catch (Exception e) {
            // Si falla, usar configuración del sistema
            return Dotenv.configure()
                    .ignoreIfMissing()
                    .systemProperties()
                    .load();
        }
    }

    /**
     * Obtiene el directorio de trabajo desde .env o usa el directorio actual.
     */
    public static String getWorkingDirectory() {
        String workDir = dotenv.get("WORK_DIR");
        if (workDir == null || workDir.isEmpty()) {
            return ".";
        }
        return workDir;
    }

    /**
     * Obtiene la ruta completa del archivo de base de datos en el directorio de trabajo.
     */
    public static String getDatabasePath() {
        String workDir = getWorkingDirectory();
        return Paths.get(workDir).resolve(CONFIG_FOLDER).resolve("edufeedai.db").toString();
    }

    /**
     * Obtiene el nombre de la carpeta de configuración.
     */
    public static String getConfigFolder() {
        return CONFIG_FOLDER;
    }

    /**
     * Obtiene la API key de OpenAI desde .env.
     */
    public static String getOpenAIApiKey() {
        return dotenv.get("OPENAI_API_KEY");
    }

    /**
     * Obtiene el intervalo de verificación de estado de batch (en segundos).
     * Por defecto: 300 segundos (5 minutos).
     */
    public static long getBatchStatusCheckInterval() {
        String interval = dotenv.get("BATCH_STATUS_CHECK_INTERVAL");
        if (interval != null && !interval.isEmpty()) {
            try {
                return Long.parseLong(interval);
            } catch (NumberFormatException e) {
                // Ignorar y usar default
            }
        }
        return 300; // 5 minutos por defecto
    }

    /**
     * Obtiene la ruta del directorio de configuración.
     */
    public static Path getConfigPath() {
        return Paths.get(getWorkingDirectory()).resolve(CONFIG_FOLDER);
    }
}

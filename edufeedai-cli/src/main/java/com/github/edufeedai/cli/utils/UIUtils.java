/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.cli.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utilidades para la interfaz de usuario en terminal.
 */
public class UIUtils {

    /**
     * Crea una barra de progreso visual.
     */
    public static String createProgressBar(int completed, int total) {
        int barLength = 10;
        if (total == 0) return "[" + " ".repeat(barLength) + "]";

        int filled = (int) ((double) completed / total * barLength);
        String bar = "●".repeat(filled) + "○".repeat(barLength - filled);
        return "[" + bar + "]";
    }

    /**
     * Formatea un timestamp Unix a formato legible.
     */
    public static String formatTimestamp(Long timestamp) {
        if (timestamp == null) return "-";
        Instant instant = Instant.ofEpochSecond(timestamp);
        ZoneId zoneId = ZoneId.systemDefault();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return instant.atZone(zoneId).format(formatter);
    }

    /**
     * Imprime una línea de separación.
     */
    public static void printSeparator() {
        System.out.println("=".repeat(60));
    }

    /**
     * Imprime una línea de guiones.
     */
    public static void printDashedLine() {
        System.out.println("-".repeat(60));
    }

    /**
     * Imprime un título centrado con separadores.
     */
    public static void printTitle(String title) {
        printSeparator();
        System.out.println(title);
        printSeparator();
    }

    /**
     * Imprime un mensaje de error.
     */
    public static void printError(String message) {
        System.err.println("Error: " + message);
    }

    /**
     * Imprime un mensaje de éxito.
     */
    public static void printSuccess(String message) {
        System.out.println("✓ " + message);
    }

    /**
     * Imprime un mensaje de advertencia.
     */
    public static void printWarning(String message) {
        System.out.println("⚠ " + message);
    }
}

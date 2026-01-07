/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.cli.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utilidades para manipulación de archivos y directorios.
 */
public class FileUtils {

    /**
     * Copia un directorio recursivamente.
     */
    public static void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(sourcePath -> {
            try {
                Path targetPath = target.resolve(source.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    if (!Files.exists(targetPath)) {
                        Files.createDirectories(targetPath);
                    }
                } else {
                    Files.copy(sourcePath, targetPath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Elimina un directorio recursivamente.
     */
    public static void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                .sorted((a, b) -> -a.compareTo(b)) // Ordenar en reversa para borrar archivos antes que directorios
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignorar errores al eliminar archivos temporales
                    }
                });
        }
    }

    /**
     * Descomprime un archivo ZIP en un directorio destino.
     * Protege contra Zip Slip vulnerability.
     */
    public static void unzip(String zipFilePath, Path destDir) throws IOException {
        Path destAbs = destDir.toAbsolutePath().normalize();
        Files.createDirectories(destAbs);

        Path zipPath = Path.of(zipFilePath);

        try (ZipFile zf = new ZipFile(zipPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();

                Path outPath = destAbs.resolve(name).normalize();
                if (!outPath.startsWith(destAbs)) {
                    throw new IOException("Zip Slip: " + name);
                }

                if (entry.isDirectory() || name.endsWith("/")) {
                    Files.createDirectories(outPath);
                    continue;
                }

                Path parent = outPath.getParent();
                if (parent != null) Files.createDirectories(parent);

                try (InputStream is = new BufferedInputStream(zf.getInputStream(entry));
                     OutputStream os = new BufferedOutputStream(
                             Files.newOutputStream(outPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
                    is.transferTo(os);
                }
            }
        }
    }

    /**
     * Extrae el Moodle Task ID del nombre del archivo ZIP.
     * Formato esperado: nombre-tarea-8608030.zip
     * Extrae el número entre el último '-' y el '.zip'
     */
    public static String extractMoodleTaskId(String zipFileName) {
        // Quitar la extensión .zip
        String nameWithoutExtension = zipFileName.replaceFirst("[.][^.]+$", "");

        // Buscar el último guion
        int lastDashIndex = nameWithoutExtension.lastIndexOf('-');

        // Si no hay guion, no hay ID de Moodle
        if (lastDashIndex == -1 || lastDashIndex == nameWithoutExtension.length() - 1) {
            return null;
        }

        // Extraer la parte después del último guion
        String potentialId = nameWithoutExtension.substring(lastDashIndex + 1);

        // Verificar que sea un número
        if (potentialId.matches("\\d+")) {
            return potentialId;
        }

        return null;
    }
}

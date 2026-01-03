/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.edufeedai.model.DigestSHA1;
public class TextSerializer {
    private static final Logger logger = LoggerFactory.getLogger(TextSerializer.class);

    /**
     * Path to the input directory containing student submissions.
     */
    private final String inputDirectory;

    /**
     * Path to the output JSONL file.
     */
    private final String outputFilePath;

    /**
     * Constructs a TextSerializer for a given task directory.
     *
     * @param taskDirectory the directory containing student submissions
     */
    public TextSerializer(String taskDirectory) {
        this.inputDirectory = taskDirectory;
        Path path = Paths.get(taskDirectory);
        this.outputFilePath = inputDirectory + File.separator +
                path.getFileName().toString().split(" ")[0] + ".jsonl";
    }

    /**
     * Deletes all JSON files in each student's submission directory within the input directory.
     * Only affects files ending with ".json".
     */
    public void deleteAllStudentJsonFiles() {
        logger.info("Eliminando archivos JSON de todas las entregas de estudiantes en {}", inputDirectory);
        File submissionsRoot = new File(inputDirectory);
        File[] studentDirs = submissionsRoot.listFiles();
        if (studentDirs == null) return;
        for (File studentDir : studentDirs) {
            if (studentDir.isDirectory() && !studentDir.isHidden()) {
                File[] jsonFiles = studentDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
                if (jsonFiles == null) continue;
                for (File jsonFile : jsonFiles) {
                    jsonFile.delete();
                }
            }
        }
    }

    /**
     * Lists all first-level directories inside the input directory.
     *
     * @return a list of first-level directory File objects
     * @throws IOException if an I/O error occurs
     */
    private List<File> listFirstLevelDirectories() throws IOException {
        
        try (Stream<Path> paths = Files.list(Paths.get(inputDirectory))) {
        return paths.filter(Files::isDirectory)
                    .filter(p -> !p.getFileName().toString().startsWith("."))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Error al listar los directorios de primer nivel en {}", inputDirectory, e);
            throw e; // Re-throw the exception to be handled by the caller
        }
    }   

    /**
     * Finds all JSON files within the input directory and its subdirectories (up to depth 2).
     *
     * @return a list of JSON File objects
     * @throws IOException if an I/O error occurs
     */
    private List<File> findAllJsonFilesExceptIDMapJSON() throws IOException {
        
        
        List<Path> fitxersJson = Files.list(Path.of(inputDirectory))
                .filter(Files::isDirectory)
                .flatMap(directoriFill -> {
                    try {
                        return Files.list(directoriFill)
                                .filter(p -> p.toString().endsWith(".json"))
                                .limit(1); // opcional: si només en vols un per carpeta
                    } catch (IOException e) {
                        return Stream.empty(); // ignora errors de lectura
                    }
                })
                .collect(Collectors.toList());
        
        return fitxersJson.stream()
               .map(Path::toFile)
               .collect(Collectors.toList());

    }

    /**
     * Concatenates the contents of the given files, separating each file's content by a line separator.
     *
     * @param files the list of files to concatenate
     * @return the concatenated string content
     * @throws IOException if an I/O error occurs
     */
    private String concatenateFileContents(List<File> files) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (File file : files) {
            sb.append(Files.readString(file.toPath()))
              .append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * Writes the given content to a file at the specified output path.
     *
     * @param content        the content to write
     * @param outputFilePath the path to the output file
     * @throws IOException if an I/O error occurs
     */
    private static void writeJsonlFile(String content, String outputFilePath) throws IOException {
        Files.write(Paths.get(outputFilePath), content.getBytes());
    }

    /**
     * Generates a single JSONL file from all JSON files found in the input directory.
     *
     * @throws IOException if an I/O error occurs
     */
    public void generateJsonl() throws IOException {
        logger.info("Generando archivo JSONL único para {}", inputDirectory);
        List<File> jsonFiles = findAllJsonFilesExceptIDMapJSON();
        generateJsonlVolumes(jsonFiles, jsonFiles.size());
    }

    /**
     * Generates multiple JSONL files, each containing up to {@code jsonsPerVolume} JSON files.
     *
     * @param jsonsPerVolume the maximum number of JSON files per JSONL volume
     * @throws IOException if an I/O error occurs
     */
    public void generateJsonl(int jsonsPerVolume) throws IOException {
        logger.info("Generando archivos JSONL por volumen en {}", inputDirectory);
        List<File> jsonFiles = findAllJsonFilesExceptIDMapJSON();
        generateJsonlVolumes(jsonFiles, jsonsPerVolume);
    }

    /**
     * Helper method to generate JSONL volumes from a list of files.
     *
     * @param jsonFiles list of JSON files
     * @param jsonsPerVolume max number of files per volume
     * @throws IOException if an I/O error occurs
     */
    private void generateJsonlVolumes(List<File> jsonFiles, int jsonsPerVolume) throws IOException {
        int totalFiles = jsonFiles.size();
        int totalVolumes = totalFiles / jsonsPerVolume + (totalFiles % jsonsPerVolume == 0 ? 0 : 1);
        File outputFile = new File(outputFilePath);
        File outputDir = outputFile.getParentFile();
        String baseName = removeExtension(outputFile.getName());
        String extension = getExtension(outputFile.getName());
        for (int i = 0; i < totalFiles; i += jsonsPerVolume) {
            List<File> currentVolume = jsonFiles.subList(i, Math.min(i + jsonsPerVolume, totalFiles));
            String content = concatenateFileContents(currentVolume);
            String volumeFileName = String.format("%svol%dde%d.%s", baseName, (i + jsonsPerVolume) / jsonsPerVolume, totalVolumes, extension);
            File volumeFile = new File(outputDir, volumeFileName);
            writeJsonlFile(content, volumeFile.toString());
        }
    }

    /**
     * Removes the extension from a file name.
     *
     * @param fileName the file name
     * @return the file name without its extension
     */
    private static String removeExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return fileName;
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, lastDotIndex);
    }

    /**
     * Gets the extension from a file name.
     *
     * @param fileName the file name
     * @return the extension, or the original file name if no extension is found
     */
    private static String getExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return fileName;
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return fileName;
        }
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * Packages all first-level directories by serializing their contents using FileConcatenator.
     *
     * @param instructions instructions to pass to the FileConcatenator serializer
     * @throws IOException if an I/O error occurs
     */
    public void packageFiles(String instructions) throws IOException {
        logger.info("Empaquetando archivos de directorios de primer nivel en {}", inputDirectory);
        List<File> directories = listFirstLevelDirectories();
        for (File dir : directories) {
            FileConcatenator fileConcatenation = new FileConcatenator(dir.getAbsolutePath(), new DigestSHA1());
            fileConcatenation.serializeToJson(instructions);
        }
    }
}

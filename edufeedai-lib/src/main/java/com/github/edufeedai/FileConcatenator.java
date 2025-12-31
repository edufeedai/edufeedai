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

package com.github.edufeedai;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.edufeedai.model.Digest;
import com.github.edufeedai.model.openai.platform.JSONLine;
import com.github.edufeedai.model.openai.platform.response.Body;
import com.github.edufeedai.model.openai.platform.response.Message;
import com.google.gson.Gson;

/**
 * Utility class for recursively concatenating the contents of files in a directory,
 * filtering out unwanted files, and serializing the result as a JSON line for OpenAI platform usage.
 *
 * <p>This class implements {@link FilenameFilter} to control which files are included.
 * It generates a unique output file name based on a digest of the input directory name.</p>
 */
public class FileConcatenator implements FilenameFilter {

    private static final Logger logger = LoggerFactory.getLogger(FileConcatenator.class);

    /** Directory to scan for input files. */
    private final String inputDirectory;
    /** Output file name (full path) for the concatenated result. */
    private final String outputFilePath;
    /** SHA-1 digest string used for output file naming. */
    private final String sha1Digest;

    /**
     * Constructs a FileConcatenator for the given directory and digest generator.
     *
     * @param inputDirectory the directory to scan for files
     * @param digest         the digest generator to create a unique output file name
     */
    public FileConcatenator(String inputDirectory, Digest digest) {
        this.inputDirectory = inputDirectory;
        String sha1;
        Path path = Paths.get(inputDirectory);
        try {
            sha1 = digest.digest(path.getFileName().toString());
        } catch (DigestException e) {
            logger.error("Error generating digest for directory name: {}", inputDirectory, e);
            sha1 = "errorgeneratingname";
        } 
        this.sha1Digest = sha1;
        this.outputFilePath = inputDirectory + File.separator + sha1Digest + ".json";
        logger.info("FileConcatenator initialized for directory: {}. Output file: {}", inputDirectory, outputFilePath);
    }

    /**
     * Recursively lists all files in the given directory that are accepted by this filter.
     *
     * @param directory the directory to scan
     * @return a list of accepted files (recursively)
     */
    private List<File> listAllAcceptedFiles(File directory) {
        List<File> files = new ArrayList<>();
        File[] children = directory.listFiles(this);
        if (children != null) {
            for (File file : children) {
                if (file.isDirectory()) {
                    files.addAll(listAllAcceptedFiles(file));
                } else if (file.isFile()) {
                    files.add(file);
                }
            }
        }
        return files;
    }

    /**
     * Lists all accepted files in the input directory (recursively).
     *
     * @return a list of accepted files
     */
    private List<File> listAllAcceptedFiles() {
        File inputDir = new File(inputDirectory);
        logger.debug("Listing all accepted files in directory: {}", inputDirectory);
        return listAllAcceptedFiles(inputDir);
    }

    /**
     * Concatenates the contents of the given files, wrapping each file's content with markers.
     *
     * @param files the list of files to concatenate
     * @return a string with all file contents concatenated and marked
     * @throws IOException if reading any file fails
     */
    private String concatenateFileContents(List<File> files) throws IOException {
        StringBuilder sb = new StringBuilder();
        String lineSeparator = System.lineSeparator();
        for (File file : files) {
            logger.debug("Concatenating file: {}", file.getAbsolutePath());
            sb.append(">>>{")
              .append(file.getName())
              .append("} ")
              .append(lineSeparator);
            sb.append(new String(Files.readAllBytes(file.toPath())))
              .append(lineSeparator)
              .append("<<<{")
              .append(file.getName())
              .append("}")
              .append(lineSeparator);
        }
        return sb.toString();
    }

    /**
     * Serializes the concatenated file contents and instructions into a JSON line file for OpenAI platform.
     *
     * @param instructions the system instructions to include as the first message
     * @throws IOException if file writing fails
     */
    protected void serializeToJson(String instructions) throws IOException {
        logger.info("Starting serialization to JSON for directory: {}", inputDirectory);
        List<File> files = listAllAcceptedFiles();
        String content = concatenateFileContents(files);
        JSONLine jsonLine = buildJSONLine(content, instructions);
        writeJsonLineToFile(jsonLine);
        logger.info("Serialization complete. Output written to: {}", outputFilePath);
    }

    /**
     * Builds a JSONLine object for OpenAI platform.
     *
     * @param content concatenated file content
     * @param instructions system instructions
     * @return JSONLine object ready for serialization
     */
    private JSONLine buildJSONLine(String content, String instructions) {
        JSONLine jsonLine = new JSONLine();
        jsonLine.setCustom_id(sha1Digest);
        jsonLine.setMethod("POST");
        jsonLine.setUrl("/v1/chat/completions");

        Body body = new Body();
        body.setModel("gpt-4o");

        Message[] messages = new Message[2];
        messages[0] = new Message();
        messages[0].setRole("system");
        messages[0].setContent(instructions);
        messages[0].setMax_tokens(1000);
        messages[1] = new Message();
        messages[1].setRole("user");
        messages[1].setContent(content);
        messages[1].setMax_tokens(1000);

        body.setMessages(messages);
        jsonLine.setBody(body);
        return jsonLine;
    }

    /**
     * Writes the JSONLine object to the output file as JSON.
     *
     * @param jsonLine the JSONLine object to write
     * @throws IOException if file writing fails
     */
    private void writeJsonLineToFile(JSONLine jsonLine) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(jsonLine);
        Files.write(Paths.get(outputFilePath), json.getBytes());
        logger.debug("JSON written to file: {}", outputFilePath);
    }

    /**
     * Determines whether a file should be accepted for concatenation based on its name and extension.
     *
     * <p>Excludes hidden files, certain system files, and files with specific extensions.</p>
     *
     * @param dir  the directory in which the file was found
     * @param name the name of the file
     * @return true if the file should be included; false otherwise
     */
    @Override
    public boolean accept(File dir, String name) {
        File candidate = new File(dir, name);
        if (candidate.isHidden()) {
            logger.trace("File {} is hidden, skipping.", name);
            return false;
        }
        if (name.equals("git-book")) {
            logger.trace("File {} is git-book, skipping.", name);
            return false;
        }
        // Exclude specific file names
        switch (name) {
            case "package.json":
            case "package-lock.json":
            case "node_modules":
            case "tsconfig.json":
            case "app.js":
            case "LICENSE":
            case "Dockerfile":
                logger.trace("File {} is excluded by name.", name);
                return false;
            default:
                break;
        }
        // Exclude files by extension
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0 && lastDot < name.length() - 1) {
            String ext = name.substring(lastDot + 1).toLowerCase();
            switch (ext) {
                case "jpg":
                case "jpeg":
                case "png":
                case "gif":
                case "bmp":
                case "tiff":
                case "svg":
                case "webp":
                case "ico":
                case "mp4":
                case "webm":
                case "ogg":
                case "mp3":
                case "wav":
                case "flac":
                case "aac":
                case "wma":
                case "m4a":
                case "opus":
                case "pdf":
                case "doc":
                case "docx":
                case "xls":
                case "xlsx":
                case "ppt":
                case "pptx":
                case "zip":
                case "rar":
                case "tar":
                case "gz":
                case "7z":
                case "bz2":
                case "xz":
                case "xml":
                    logger.trace("File {} is excluded by extension .{}.", name, ext);
                    return false;
                default:
                    break;
            }
        }
        return true;
    }
}

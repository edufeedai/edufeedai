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

package com.github.edufeedai.model;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.edufeedai.model.exceptions.PDFExtractTextAndImageException;
import com.github.edufeedai.model.ocrlib.OCRProcessor;

/**
 * Procesador de archivos que detecta tipos de archivo y extrae su contenido.
 * Soporta archivos de texto plano y PDFs.
 */
public class FileProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessor.class);
    private final Tika tika;

    // Tipos MIME soportados para texto plano
    private static final List<String> TEXT_MIME_TYPES = Arrays.asList(
        "text/plain",
        "text/x-java-source",
        "text/x-python",
        "text/x-c",
        "text/x-c++",
        "text/html",
        "text/css",
        "text/javascript",
        "application/javascript",
        "application/x-javascript",
        "text/x-sql",
        "application/sql",
        "text/markdown",
        "text/x-markdown",
        "application/json",
        "application/xml",
        "text/xml"
    );

    // Tipos MIME soportados para PDFs
    private static final String PDF_MIME_TYPE = "application/pdf";

    /**
     * Resultado del procesamiento de un archivo.
     */
    public static class FileProcessingResult {
        private final File file;
        private final String mimeType;
        private final ProcessingType processingType;
        private final String extractedText;
        private final File extractedFile; // Para PDFs procesados con OCR

        public enum ProcessingType {
            TEXT_PLAIN,      // Texto plano leído directamente
            PDF_EXTRACTED,   // PDF procesado con OCR
            PDF_ORIGINAL,    // PDF original (sin procesar)
            UNSUPPORTED      // Tipo no soportado
        }

        public FileProcessingResult(File file, String mimeType, ProcessingType processingType,
                                   String extractedText, File extractedFile) {
            this.file = file;
            this.mimeType = mimeType;
            this.processingType = processingType;
            this.extractedText = extractedText;
            this.extractedFile = extractedFile;
        }

        public File getFile() { return file; }
        public String getMimeType() { return mimeType; }
        public ProcessingType getProcessingType() { return processingType; }
        public String getExtractedText() { return extractedText; }
        public File getExtractedFile() { return extractedFile; }
        public boolean isSupported() { return processingType != ProcessingType.UNSUPPORTED; }
    }

    /**
     * Crea un nuevo procesador de archivos.
     */
    public FileProcessor() {
        this.tika = new Tika();
        logger.info("FileProcessor inicializado con Apache Tika");
    }

    /**
     * Detecta el tipo MIME de un archivo.
     * @param file Archivo a analizar
     * @return Tipo MIME detectado
     * @throws IOException Si ocurre un error al leer el archivo
     */
    public String detectMimeType(File file) throws IOException {
        String mimeType = tika.detect(file);
        logger.debug("Tipo MIME detectado para {}: {}", file.getName(), mimeType);
        return mimeType;
    }

    /**
     * Procesa un archivo y extrae su contenido según su tipo.
     * @param file Archivo a procesar
     * @return Resultado del procesamiento
     * @throws IOException Si ocurre un error durante el procesamiento
     */
    public FileProcessingResult processFile(File file) throws IOException {
        String mimeType = detectMimeType(file);

        // Verificar si es texto plano
        if (isTextFile(mimeType)) {
            logger.info("Procesando archivo de texto: {}", file.getName());
            String content = readTextFile(file);
            return new FileProcessingResult(file, mimeType,
                FileProcessingResult.ProcessingType.TEXT_PLAIN, content, null);
        }

        // Verificar si es PDF
        if (isPdfFile(mimeType)) {
            logger.info("Detectado archivo PDF: {}", file.getName());
            // Por ahora, devolvemos el PDF original sin procesar
            // El procesamiento OCR se hará opcionalmente más adelante
            return new FileProcessingResult(file, mimeType,
                FileProcessingResult.ProcessingType.PDF_ORIGINAL, null, file);
        }

        // Tipo no soportado
        logger.warn("Tipo de archivo no soportado: {} (MIME: {})", file.getName(), mimeType);
        return new FileProcessingResult(file, mimeType,
            FileProcessingResult.ProcessingType.UNSUPPORTED, null, null);
    }

    /**
     * Procesa un archivo PDF y extrae su contenido usando OCR.
     * @param file Archivo PDF a procesar
     * @param ocrProcessor Procesador OCR a utilizar
     * @return Resultado del procesamiento con texto extraído
     * @throws IOException Si ocurre un error durante el procesamiento
     * @throws PDFExtractTextAndImageException Si ocurre un error durante la extracción
     */
    public FileProcessingResult processPdfWithOcr(File file, OCRProcessor ocrProcessor)
            throws IOException, PDFExtractTextAndImageException {
        String mimeType = detectMimeType(file);

        if (!isPdfFile(mimeType)) {
            throw new IOException("El archivo no es un PDF: " + file.getName());
        }

        logger.info("Procesando PDF con OCR: {}", file.getName());

        // Usar PDFExtractTextAndImagesOrdered para extraer contenido
        PDFExtractTextAndImagesOrdered extractor = new PDFExtractTextAndImagesOrdered(ocrProcessor);
        extractor.extractImagesAndTextFromPDFFile(file.getAbsolutePath());

        // Obtener el archivo .txt generado
        String txtFilePath = file.getAbsolutePath().replaceFirst("[.][^.]+$", ".txt");
        File txtFile = new File(txtFilePath);

        if (!txtFile.exists()) {
            throw new IOException("No se generó el archivo de texto extraído: " + txtFilePath);
        }

        // Leer el contenido extraído
        String extractedText = readTextFile(txtFile);

        return new FileProcessingResult(file, mimeType,
            FileProcessingResult.ProcessingType.PDF_EXTRACTED, extractedText, txtFile);
    }

    /**
     * Extrae texto de un PDF usando OCRMyPDF y PDFBox.
     * Guarda el PDF original en .edufeedai/originals antes de procesarlo.
     * @param file Archivo PDF a procesar
     * @param moodleTaskId ID de la tarea de Moodle (puede ser null)
     * @param studentName Nombre del estudiante
     * @param workingDirectory Directorio de trabajo donde está .edufeedai
     * @return Resultado del procesamiento con texto extraído
     * @throws IOException Si ocurre un error durante la extracción
     * @throws InterruptedException Si el proceso OCR es interrumpido
     */
    public FileProcessingResult extractTextFromPdf(File file, String moodleTaskId, String studentName, String workingDirectory)
            throws IOException, InterruptedException {
        String mimeType = detectMimeType(file);

        if (!isPdfFile(mimeType)) {
            throw new IOException("El archivo no es un PDF: " + file.getName());
        }

        logger.info("Extrayendo texto de PDF con OCR: {}", file.getName());

        boolean ocrSuccessful = false;

        try {
            // 1. Crear directorio para guardar el original
            String taskIdFolder = (moodleTaskId != null && !moodleTaskId.isEmpty()) ? moodleTaskId : "unknown_task";
            Path originalsDir = Path.of(workingDirectory, ".edufeedai", "originals", taskIdFolder, studentName);
            Files.createDirectories(originalsDir);

            // 2. Guardar copia del PDF original
            Path originalBackup = originalsDir.resolve(file.getName());
            logger.debug("Guardando copia original en: {}", originalBackup);
            Files.copy(file.toPath(), originalBackup, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // 3. Procesar PDF con OCRMyPDF (genera {nombre}.ocr.pdf)
            logger.info("Ejecutando OCRMyPDF sobre: {}", file.getName());
            Path ocrPdfPath = com.github.edufeedai.model.ocrlib.OCRMyPDF.ocrAndOptimize(file.toPath());

            // 4. Reemplazar el PDF original con la versión OCR
            logger.debug("Reemplazando PDF original con versión OCR");
            Files.move(ocrPdfPath, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            ocrSuccessful = true;

        } catch (RuntimeException e) {
            // Detectar si el error es por falta de OCRMyPDF instalado
            if (e.getMessage() != null && e.getMessage().contains("Command failed")) {
                logger.warn("╔══════════════════════════════════════════════════════════════╗");
                logger.warn("║  ADVERTENCIA: OCRMyPDF no está instalado                    ║");
                logger.warn("╠══════════════════════════════════════════════════════════════╣");
                logger.warn("║  Continuando sin OCR - usando extracción básica de texto    ║");
                logger.warn("║                                                              ║");
                logger.warn("║  Para mejor calidad, instala OCRMyPDF:                       ║");
                logger.warn("║                                                              ║");
                logger.warn("║  Ubuntu/Debian:                                              ║");
                logger.warn("║    sudo apt-get install ocrmypdf                             ║");
                logger.warn("║                                                              ║");
                logger.warn("║  macOS:                                                      ║");
                logger.warn("║    brew install ocrmypdf                                     ║");
                logger.warn("║                                                              ║");
                logger.warn("║  Python pip:                                                 ║");
                logger.warn("║    pip install ocrmypdf                                      ║");
                logger.warn("║                                                              ║");
                logger.warn("║  Más información: https://ocrmypdf.readthedocs.io/          ║");
                logger.warn("╚══════════════════════════════════════════════════════════════╝");

                ocrSuccessful = false;
            } else {
                // Re-lanzar otros errores de runtime
                throw e;
            }
        }

        // 5. Extraer texto del PDF (con o sin OCR)
        String extractionMethod = ocrSuccessful ? "PDF procesado con OCR" : "PDF sin OCR (básico)";
        logger.info("Extrayendo texto del {}: {}", extractionMethod, file.getName());

        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(document);

            logger.debug("Texto extraído de {}: {} caracteres", file.getName(), extractedText.length());

            return new FileProcessingResult(file, mimeType,
                FileProcessingResult.ProcessingType.PDF_EXTRACTED, extractedText, null);
        }
    }

    /**
     * Obtiene todos los archivos de un directorio de forma recursiva.
     * @param directory Directorio a escanear
     * @return Lista de archivos encontrados
     * @throws IOException Si ocurre un error al leer el directorio
     */
    public List<File> getAllFiles(File directory) throws IOException {
        List<File> files = new ArrayList<>();

        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("El directorio no existe o no es válido: " + directory);
        }

        Files.walk(directory.toPath())
            .filter(Files::isRegularFile)
            .filter(path -> !isHiddenOrSystemFile(path))
            .forEach(path -> files.add(path.toFile()));

        logger.info("Encontrados {} archivos en {}", files.size(), directory.getAbsolutePath());
        return files;
    }

    /**
     * Verifica si un tipo MIME corresponde a un archivo de texto plano.
     */
    private boolean isTextFile(String mimeType) {
        return TEXT_MIME_TYPES.stream()
            .anyMatch(type -> mimeType.equals(type) || mimeType.startsWith(type));
    }

    /**
     * Verifica si un tipo MIME corresponde a un archivo PDF.
     */
    private boolean isPdfFile(String mimeType) {
        return PDF_MIME_TYPE.equals(mimeType);
    }

    /**
     * Lee el contenido de un archivo de texto.
     */
    private String readTextFile(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Verifica si un archivo es oculto o del sistema (debe ser ignorado).
     */
    private boolean isHiddenOrSystemFile(Path path) {
        String fileName = path.getFileName().toString();
        // Ignorar archivos ocultos (comienzan con .)
        if (fileName.startsWith(".")) {
            return true;
        }
        // Ignorar archivos del sistema comunes
        return fileName.equals("Thumbs.db") ||
               fileName.equals("Desktop.ini") ||
               fileName.equals(".DS_Store");
    }
}

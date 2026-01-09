/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Extrae imágenes de archivos PDF usando Apache PDFBox.
 */
public class PDFImageExtractor extends PDFStreamEngine {

    private static final Logger logger = LoggerFactory.getLogger(PDFImageExtractor.class);

    private final List<ExtractedImage> extractedImages;
    private final Path outputDirectory;
    private final ImageDeduplicator deduplicator;
    private int currentPageNumber;
    private int imageIndexInPage;

    /**
     * Crea un extractor de imágenes.
     * @param outputDirectory Directorio donde guardar las imágenes extraídas
     */
    public PDFImageExtractor(Path outputDirectory) {
        this.extractedImages = new ArrayList<>();
        this.outputDirectory = outputDirectory;
        this.deduplicator = new ImageDeduplicator();
        this.currentPageNumber = 0;
        this.imageIndexInPage = 0;
    }

    /**
     * Extrae todas las imágenes de un PDF a una subcarpeta.
     * @param pdfPath Ruta del archivo PDF
     * @return Lista de imágenes extraídas con metadata
     * @throws IOException Si ocurre un error durante la extracción
     */
    public static List<ExtractedImage> extractImages(Path pdfPath) throws IOException {
        // Crear subcarpeta para imágenes
        String pdfName = pdfPath.getFileName().toString().replaceFirst("[.][^.]+$", "");
        Path imagesDir = pdfPath.getParent().resolve(pdfName + "_images");
        Files.createDirectories(imagesDir);

        logger.info("Extrayendo imágenes de PDF: {} -> {}", pdfPath, imagesDir);

        PDFImageExtractor extractor = new PDFImageExtractor(imagesDir);

        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            int pageNum = 1;
            for (PDPage page : document.getPages()) {
                extractor.setCurrentPage(pageNum);
                extractor.processPage(page);
                pageNum++;
            }
        }

        logger.info("Extracción completada: {} imágenes extraídas de {}",
                   extractor.extractedImages.size(), pdfPath.getFileName());

        return extractor.extractedImages;
    }

    /**
     * Establece el número de página actual para la extracción.
     */
    private void setCurrentPage(int pageNumber) {
        this.currentPageNumber = pageNumber;
        this.imageIndexInPage = 0;
    }

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
        if ("Do".equals(operator.getName())) {
            COSName objectName = (COSName) operands.get(0);
            PDXObject xobject = getResources().getXObject(objectName);

            if (xobject instanceof PDImageXObject) {
                PDImageXObject image = (PDImageXObject) xobject;
                imageIndexInPage++;
                processImage(image);
            }
        } else {
            super.processOperator(operator, operands);
        }
    }

    /**
     * Procesa y guarda una imagen extraída del PDF.
     */
    private void processImage(PDImageXObject image) throws IOException {
        BufferedImage bufferedImage = image.getImage();

        // Determinar formato de la imagen
        String suffix = image.getSuffix();
        if (suffix == null || suffix.isEmpty()) {
            suffix = "png";
        }

        // Generar nombre de archivo: page_{num}_img_{index}.{ext}
        String filename = String.format("page_%d_img_%d.%s",
                                       currentPageNumber, imageIndexInPage, suffix);
        Path imagePath = outputDirectory.resolve(filename);

        // Guardar imagen
        ImageIO.write(bufferedImage, suffix, imagePath.toFile());

        // Obtener metadata
        long fileSize = Files.size(imagePath);
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        String mimeType = getMimeType(suffix);

        // Crear ruta relativa (nombre de carpeta + nombre de archivo)
        String relativePath = outputDirectory.getFileName().toString() + "/" + filename;

        // Calcular hashes perceptuales
        String[] hashes = null;
        try {
            hashes = deduplicator.calculateHashes(imagePath);
        } catch (IOException e) {
            logger.warn("No se pudo calcular hash de imagen: {}", imagePath, e);
        }

        // Crear objeto ExtractedImage
        ExtractedImage extractedImage = new ExtractedImage(
            relativePath,
            mimeType,
            currentPageNumber,
            imageIndexInPage,
            fileSize,
            width,
            height
        );

        // Asignar hashes si se calcularon correctamente
        if (hashes != null && hashes.length == 2) {
            extractedImage.setDHash(hashes[0]);
            extractedImage.setPHash(hashes[1]);
        }

        extractedImages.add(extractedImage);

        logger.debug("Imagen extraída: {}", extractedImage);
    }

    /**
     * Obtiene el MIME type basado en la extensión del archivo.
     */
    private String getMimeType(String extension) {
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "tiff":
            case "tif":
                return "image/tiff";
            default:
                return "image/" + extension.toLowerCase();
        }
    }
}

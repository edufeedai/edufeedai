/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model;

import com.github.edufeedai.model.exceptions.PDFExtractTextAndImageException;
import com.github.edufeedai.model.ocrlib.OCRProcessor;
import com.github.edufeedai.model.ocrlib.OCRProcessorException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Extrae texto y realiza OCR sobre imágenes de un PDF, manteniendo el orden de aparición.
 * Los bloques de contenido extraídos (texto e imágenes) se almacenan en una lista y pueden guardarse en un archivo.
 */
public class PDFExtractTextAndImagesOrdered extends PDFStreamEngine {

    private static final Logger logger = LoggerFactory.getLogger(PDFExtractTextAndImagesOrdered.class);
    private final OCRProcessor ocrProcessor;
    private final List<ContentBlock> contentBlocks;

    /**
     * Crea un extractor de texto e imágenes para PDFs.
     * @param processor Procesador OCR a utilizar para imágenes
     * @throws PDFExtractTextAndImageException Si ocurre un error al inicializar
     */
    public PDFExtractTextAndImagesOrdered(OCRProcessor processor) throws PDFExtractTextAndImageException {
        this.ocrProcessor = processor;
        this.contentBlocks = new ArrayList<>();
        logger.info("PDFExtractTextAndImagesOrdered inicializado con OCRProcessor: {}", processor.getClass().getSimpleName());
    }

    /**
     * Procesa operadores de PDF para extraer imágenes y realizar OCR sobre ellas.
     */
    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
        if ("Do".equals(operator.getName())) {
            COSName objectName = (COSName) operands.get(0);
            PDXObject xobject = getResources().getXObject(objectName);
            if (xobject instanceof PDImageXObject) {
                logger.debug("Procesando imagen en PDF: {}", objectName.getName());
                processImage((PDImageXObject) xobject);
            }
        } else {
            super.processOperator(operator, operands);
        }
    }

    /**
     * Extrae texto de una página y lo añade como bloque de contenido.
     * @param page Página PDF
     * @throws IOException Si ocurre un error al extraer texto
     */
    public void extractTextFromPage(PDPage page) throws IOException {
        logger.debug("Extrayendo texto de la página PDF");
        PDFTextStripperByArea textStripper = new PDFTextStripperByArea();
        textStripper.setSortByPosition(true);
        Rectangle rect = new Rectangle(0, 0, (int) page.getMediaBox().getWidth(), (int) page.getMediaBox().getHeight());
        textStripper.addRegion("pageRegion", rect);
        textStripper.extractRegions(page);
        String pageText = textStripper.getTextForRegion("pageRegion");
        contentBlocks.add(new ContentBlock("text", pageText));
    }

    /**
     * Devuelve la lista de bloques de contenido extraídos.
     * @return Lista de bloques de contenido
     */
    public List<ContentBlock> getContentBlocks() {
        return contentBlocks;
    }

    /**
     * Extrae texto e imágenes de un PDF y guarda los bloques en un archivo de texto.
     * @param pdfFilePath Ruta del archivo PDF
     * @throws PDFExtractTextAndImageException Si ocurre un error durante la extracción o guardado
     */
    public void extractImagesAndTextFromPDFFile(String pdfFilePath) throws PDFExtractTextAndImageException {
        File file = new File(pdfFilePath);
        String outputFileName = file.getAbsolutePath().replaceFirst("[.][^.]+$", ".txt");
        logger.info("Iniciando extracción de texto e imágenes del PDF: {}", pdfFilePath);
        try (PDDocument document = Loader.loadPDF(file)) {
            for (PDPage page : document.getPages()) {
                extractTextFromPage(page);
                processPage(page);
            }
            saveContentBlocksToFile(outputFileName);
            logger.info("Extracción y guardado completados en: {}", outputFileName);
        } catch (IOException e) {
            logger.error("Error extrayendo texto/imágenes del PDF: {}", pdfFilePath, e);
            throw new PDFExtractTextAndImageException(e);
        }
    }

    /**
     * Procesa una imagen extraída del PDF, realiza OCR y la añade como bloque de contenido.
     * @param image Imagen PDF extraída
     * @throws IOException Si ocurre un error de E/S
     */
    private void processImage(PDImageXObject image) throws IOException {
        File tempImageFile = File.createTempFile("pdf_image_", ".png");
        try {
            ImageIO.write(image.getImage(), "png", tempImageFile);
            String ocrResult = ocrProcessor.performOCR(tempImageFile);
            contentBlocks.add(new ContentBlock("image", ocrResult));
            logger.debug("OCR realizado sobre imagen y añadido como bloque de contenido.");
        } catch (OCRProcessorException e) {
            logger.error("Error realizando OCR sobre imagen.", e);
            throw new IOException(e);
        } finally {
            Files.deleteIfExists(tempImageFile.toPath());
        }
    }

    /**
     * Guarda los bloques de contenido en un archivo de texto.
     * @param outputFileName Nombre del archivo de salida
     * @throws PDFExtractTextAndImageException Si ocurre un error al guardar
     */
    private void saveContentBlocksToFile(String outputFileName) throws PDFExtractTextAndImageException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(outputFileName), true)) {
            for (ContentBlock block : contentBlocks) {
                pw.println(block);
            }
            logger.info("Bloques de contenido guardados en: {}", outputFileName);
        } catch (IOException e) {
            logger.error("Error guardando bloques de contenido en fichero: {}", outputFileName, e);
            throw new PDFExtractTextAndImageException(e);
        }
    }
}

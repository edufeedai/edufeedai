/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Detecta imágenes repetidas (logos/banners/cabeceras) usando perceptual hashing.
 *
 * Implementa algoritmos simplificados de dHash y pHash sin dependencias externas.
 *
 * Umbrales:
 * - dHash distance <= 4 AND pHash distance <= 6 → mismo cluster
 * - Cluster size >= 3 → imagenes repetidas (banners, logos, etc.)
 */
public class ImageDeduplicator {

    private static final Logger logger = LoggerFactory.getLogger(ImageDeduplicator.class);

    // Umbrales de similitud (ajustar según pruebas reales)
    private static final int DHASH_MAX_DIST = 4;
    private static final int PHASH_MAX_DIST = 6;
    private static final int MIN_CLUSTER_SIZE_TO_MARK_DUPLICATE = 3;

    private static final int HASH_SIZE = 8; // 8x8 = 64 bits

    public ImageDeduplicator() {
        logger.info("ImageDeduplicator inicializado con dHash({}) y pHash({})", HASH_SIZE, HASH_SIZE);
    }

    /**
     * Calcula los hashes perceptuales de una imagen.
     * @param imagePath Ruta de la imagen
     * @return Array con [dHash, pHash] como strings hexadecimales
     */
    public String[] calculateHashes(Path imagePath) throws IOException {
        BufferedImage img = ImageIO.read(imagePath.toFile());
        if (img == null) {
            throw new IOException("No se pudo cargar la imagen: " + imagePath);
        }

        String dHash = calculateDifferenceHash(img);
        String pHash = calculatePerceptiveHash(img);

        return new String[] { dHash, pHash };
    }

    /**
     * Calcula Difference Hash (dHash): compara píxeles adyacentes.
     * Algoritmo:
     * 1. Redimensionar a 9x8 (72 píxeles)
     * 2. Convertir a escala de grises
     * 3. Comparar cada píxel con su vecino derecho
     * 4. Si pixel[x] > pixel[x+1] → bit 1, sino → bit 0
     */
    private String calculateDifferenceHash(BufferedImage img) {
        // Redimensionar a 9x8
        BufferedImage resized = resize(img, HASH_SIZE + 1, HASH_SIZE);

        // Convertir a escala de grises y calcular hash
        StringBuilder hash = new StringBuilder();
        for (int y = 0; y < HASH_SIZE; y++) {
            for (int x = 0; x < HASH_SIZE; x++) {
                int pixel1 = getGrayscale(resized.getRGB(x, y));
                int pixel2 = getGrayscale(resized.getRGB(x + 1, y));
                hash.append(pixel1 > pixel2 ? '1' : '0');
            }
        }

        // Convertir binario a hexadecimal
        return binaryToHex(hash.toString());
    }

    /**
     * Calcula Perceptive Hash (pHash): usa DCT (Discrete Cosine Transform) simplificado.
     * Algoritmo simplificado:
     * 1. Redimensionar a 32x32
     * 2. Convertir a escala de grises
     * 3. Calcular promedio de píxeles
     * 4. Comparar cada píxel con el promedio
     */
    private String calculatePerceptiveHash(BufferedImage img) {
        // Redimensionar a 32x32 para mejor precisión
        BufferedImage resized = resize(img, 32, 32);

        // Convertir a escala de grises y calcular promedio
        double sum = 0;
        int[][] pixels = new int[32][32];
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                pixels[y][x] = getGrayscale(resized.getRGB(x, y));
                sum += pixels[y][x];
            }
        }
        double average = sum / (32 * 32);

        // Calcular hash: 1 si pixel > average, 0 si no
        // Usar solo región central 8x8 para hash final
        StringBuilder hash = new StringBuilder();
        for (int y = 12; y < 20; y++) {
            for (int x = 12; x < 20; x++) {
                hash.append(pixels[y][x] > average ? '1' : '0');
            }
        }

        return binaryToHex(hash.toString());
    }

    /**
     * Redimensiona una imagen.
     */
    private BufferedImage resize(BufferedImage img, int width, int height) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    /**
     * Obtiene el valor de escala de grises de un píxel RGB.
     */
    private int getGrayscale(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r + g + b) / 3;
    }

    /**
     * Convierte string binario a hexadecimal.
     */
    private String binaryToHex(String binary) {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < binary.length(); i += 4) {
            String chunk = binary.substring(i, Math.min(i + 4, binary.length()));
            int value = Integer.parseInt(chunk, 2);
            hex.append(Integer.toHexString(value));
        }
        return hex.toString();
    }

    /**
     * Detecta duplicados en una lista de imágenes extraídas.
     */
    public List<ImageCluster> detectDuplicates(List<ExtractedImage> images) {
        logger.info("Detectando duplicados en {} imágenes", images.size());

        List<ImageCluster> clusters = clusterBySimilarity(images);

        // Marcar como duplicadas las imágenes en clusters grandes
        for (ImageCluster cluster : clusters) {
            boolean isDuplicate = cluster.getSize() >= MIN_CLUSTER_SIZE_TO_MARK_DUPLICATE;

            if (isDuplicate) {
                for (ExtractedImage img : cluster.getImages()) {
                    img.setDuplicate(true);
                }
                logger.info("Cluster de {} imágenes marcado como duplicado", cluster.getSize());
            }
        }

        clusters.sort(Comparator.comparingInt(ImageCluster::getSize).reversed());

        logger.info("Detección completada: {} clusters, {} imágenes duplicadas",
                   clusters.size(),
                   images.stream().filter(ExtractedImage::isDuplicate).count());

        return clusters;
    }

    /**
     * Agrupa imágenes similares en clusters.
     */
    private List<ImageCluster> clusterBySimilarity(List<ExtractedImage> images) {
        List<ImageCluster> clusters = new ArrayList<>();

        for (ExtractedImage img : images) {
            ImageCluster matchedCluster = null;

            for (ImageCluster cluster : clusters) {
                if (isSimilar(img, cluster.getRepresentative())) {
                    matchedCluster = cluster;
                    break;
                }
            }

            if (matchedCluster == null) {
                clusters.add(new ImageCluster(img));
            } else {
                matchedCluster.add(img);
            }
        }

        return clusters;
    }

    /**
     * Determina si dos imágenes son similares.
     */
    private boolean isSimilar(ExtractedImage img1, ExtractedImage img2) {
        if (img1.getDHash() == null || img1.getPHash() == null ||
            img2.getDHash() == null || img2.getPHash() == null) {
            return false;
        }

        int dDist = hammingDistance(img1.getDHash(), img2.getDHash());
        int pDist = hammingDistance(img1.getPHash(), img2.getPHash());

        return dDist <= DHASH_MAX_DIST && pDist <= PHASH_MAX_DIST;
    }

    /**
     * Calcula la distancia de Hamming entre dos hashes hexadecimales.
     */
    private int hammingDistance(String hash1, String hash2) {
        if (hash1.length() != hash2.length()) {
            return Integer.MAX_VALUE;
        }

        int distance = 0;
        for (int i = 0; i < hash1.length(); i++) {
            char c1 = hash1.charAt(i);
            char c2 = hash2.charAt(i);
            int xor = Character.digit(c1, 16) ^ Character.digit(c2, 16);
            distance += Integer.bitCount(xor);
        }

        return distance;
    }

    /**
     * Representa un cluster de imágenes similares.
     */
    public static class ImageCluster {
        private final ExtractedImage representative;
        private final List<ExtractedImage> images;

        public ImageCluster(ExtractedImage representative) {
            this.representative = representative;
            this.images = new ArrayList<>();
            this.images.add(representative);
        }

        public void add(ExtractedImage image) {
            images.add(image);
        }

        public ExtractedImage getRepresentative() {
            return representative;
        }

        public List<ExtractedImage> getImages() {
            return images;
        }

        public int getSize() {
            return images.size();
        }

        @Override
        public String toString() {
            return String.format("ImageCluster{size=%d, representative='%s', duplicate=%b}",
                    images.size(),
                    representative.getRelativePath(),
                    images.size() >= MIN_CLUSTER_SIZE_TO_MARK_DUPLICATE);
        }
    }
}

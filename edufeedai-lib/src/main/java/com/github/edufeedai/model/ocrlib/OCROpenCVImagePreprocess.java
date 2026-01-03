/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model.ocrlib;


import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


public class OCROpenCVImagePreprocess {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Cargar la librería de OpenCV
    }

    public static void Binarize(String imagePath){

        Binarize(imagePath, imagePath);

    }

    public static void Binarize(String imagePathSource, String imagePathDestination){

        Mat originalImage = Imgcodecs.imread(imagePathSource, Imgcodecs.IMREAD_GRAYSCALE);
        if (originalImage.empty()) {
            System.out.println("No se pudo cargar la imagen");
            return;
        }

        // Aplicar binarización adaptativa con un bloque más grande
        Mat binarizedImage = new Mat();
        Imgproc.adaptiveThreshold(originalImage, binarizedImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 2);

        // Guardar la imagen procesada
        Imgcodecs.imwrite(imagePathDestination, binarizedImage);



    }

}

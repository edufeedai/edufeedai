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

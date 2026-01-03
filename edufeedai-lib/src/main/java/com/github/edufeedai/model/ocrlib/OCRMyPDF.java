/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model.ocrlib;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class OCRMyPDF {

    public OCRMyPDF() {

    }

    private static void run(List<String> cmd) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(cmd)
                .redirectErrorStream(true)
                .inheritIO()
                .start();
        int code = p.waitFor();
        if (code != 0) {
            throw new RuntimeException("Command failed (" + code + "): " + String.join(" ", cmd));
        }
    }
    

    public static Path ocrAndOptimize(Path inPdf) throws IOException, InterruptedException {
        
        String base = inPdf.toString().replaceAll("(?i)\\.pdf$", "");
        Path ocr = Path.of(base + ".ocr.pdf");
        //Path opt = Path.of(base + ".ocr.opt.pdf");

        run(List.of("ocrmypdf", "--force-ocr", "--optimize", "0", "-l", "spa+eng+cat", "--output-type", "pdf",
                inPdf.toString(), ocr.toString()));

        //run(List.of("ocrmypdf", "--skip-text", "--optimize", "2", "--jpeg-quality", "60",
        //        ocr.toString(), opt.toString()));

        return ocr;
    }
    
    
}


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


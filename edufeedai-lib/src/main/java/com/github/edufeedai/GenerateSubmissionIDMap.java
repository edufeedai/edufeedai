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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestException;
import java.util.ArrayList;
import static java.util.Arrays.stream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.edufeedai.model.Digest;
import com.github.edufeedai.model.SubmissionIdMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Clase responsable de generar y guardar mapas de IDs de entregas de estudiantes
 * a partir de los nombres de carpeta en un directorio de evaluación.
 */
public class GenerateSubmissionIDMap {
    /** Ruta al directorio de la evaluación. */
    private String assessmentFolder;
    /** Objeto Digest para generar IDs únicos. */
    private Digest digest;

    /**
     * Constructor.
     * @param assessmentFolder Ruta al directorio de la evaluación
     * @param digest Objeto Digest para generar IDs únicos
     */
    public GenerateSubmissionIDMap(String assessmentFolder, Digest digest) {
        this.assessmentFolder = assessmentFolder;
        this.digest = digest;
    }

    /**
     * Genera un arreglo de SubmissionIdMap a partir de las carpetas de estudiantes.
     * @return Arreglo de SubmissionIdMap ordenado por custom_id
     */
    public SubmissionIdMap[] generateSubmissionIDMaps() {
        
        List<File> files = new ArrayList<>();

        try (Stream<Path> paths = Files.list(Paths.get(assessmentFolder))) {
        files = paths.filter(Files::isDirectory)
                    .filter(p -> !p.getFileName().toString().startsWith("."))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (Exception e) {

            e.printStackTrace();
            return new SubmissionIdMap[0];
        }
        
        if (files == null) {
            return new SubmissionIdMap[0];
        }
        SubmissionIdMap[] submissionIdMaps = new SubmissionIdMap[files.size()];
        for (int i = 0; i < files.size(); i++) {
            SubmissionIdMap submissionIdMap = new SubmissionIdMap();
            submissionIdMap.setCustom_id(files.get(i).getName());
            try {
                submissionIdMap.setSubmission_id(digest.digest(files.get(i).getName()));
            } catch (DigestException e) {
                submissionIdMap.setSubmission_id("errorgeneratingname");
            }
            submissionIdMaps[i] = submissionIdMap;
        }
        List<SubmissionIdMap> lista = stream(submissionIdMaps)
            .sorted(Comparator.comparing(SubmissionIdMap::getCustom_id))
            .toList();
        return lista.toArray(new SubmissionIdMap[0]);
    }

    /**
     * Guarda el arreglo de SubmissionIdMap en un archivo JSON dentro del directorio de evaluación.
     * @param submissionIdMaps Arreglo de mapas a guardar
     * @param assessmentIdMapFile Nombre del archivo de salida
     * @return Nombre del archivo guardado, o null si hubo error
     */
    public String saveSubmissionIDMaps(SubmissionIdMap[] submissionIdMaps, String assessmentIdMapFile) {
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
        String json = gson.toJson(submissionIdMaps);
        File file = new File(assessmentFolder + File.separator + assessmentIdMapFile);
        try {
            java.nio.file.Files.writeString(file.toPath(), json);
            return file.getName();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

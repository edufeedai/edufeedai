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

package com.github.edufeedai.model.openai.platform.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.github.edufeedai.model.openai.platform.api.exceptions.OpenAIAPIException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OpenAIFileManagement implements AutoCloseable {

    final String apiKey;
    final String url;

    private static final Logger LOGGER = Logger.getLogger(OpenAIFileManagement.class.getName());

    // Cliente reutilizable (mejor que crear uno por request)
    private final OkHttpClient client;

    public OpenAIFileManagement(String apiKey) {
        this(apiKey, "https://api.openai.com/v1/files");
    }

    public OpenAIFileManagement(String apiKey, String url) {
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey");
        this.url = Objects.requireNonNull(url, "url");

        this.client = new OkHttpClient.Builder()
                // Ajusta si subes PDFs grandes
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Uploads a file to OpenAI for batch processing.
     */
    public String uploadBatchFile(String filePath) throws OpenAIAPIException {
        File jsonlFile = new File(filePath);
        return uploadFile(jsonlFile, "batch");
    }

    /**
     * Uploads a file to OpenAI for batch processing.
     */
    public String uploadBatchFile(File file) throws OpenAIAPIException {
        return uploadFile(file, "batch");
    }

    /**
     * Uploads a student file to OpenAI for user data purposes.
     */
    public String uploadStudentFile(String filePath) throws OpenAIAPIException {
        File file = new File(filePath);
        return uploadFile(file, "user_data");
    }

    /**
     * Uploads a student file to OpenAI for user data purposes.
     */
    public String uploadStudentFile(File file) throws OpenAIAPIException {
        return uploadFile(file, "user_data");
    }

    /**
     * Uploads a file to OpenAI with a specified purpose.
     *
     * TODO: Validar tamaño máximo de archivo antes de subir (límites de OpenAI API)
     */
    private String uploadFile(File file, String purpose) throws OpenAIAPIException {
        try {
            if (file == null || !file.exists() || !file.isFile()) {
                throw new OpenAIAPIException("El archivo no existe o no es un fichero válido: " + file);
            }

            // Intenta deducir mime real (por si es PDF, JSONL, etc.)
            // Si falla, cae a application/octet-stream.
            String detected = null;
            try {
                detected = Files.probeContentType(file.toPath());
            } catch (Exception ignore) {
                // no pasa nada
            }

            MediaType mediaType = MediaType.parse(detected != null ? detected : "application/octet-stream");

            RequestBody fileBody = RequestBody.create(file, mediaType);

            // Multipart correcto: OkHttp generará "multipart/form-data; boundary=..."
            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("purpose", purpose)
                    .addFormDataPart("file", file.getName(), fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    // No pongas Content-Type manualmente: OkHttp lo hace bien
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                ResponseBody body = response.body();
                String responseBodyStr = body != null ? body.string() : "";

                if (response.isSuccessful()) {
                    JSONObject jsonResponse = new JSONObject(responseBodyStr);
                    String id = jsonResponse.getString("id");
                    LOGGER.info(() -> String.format("Archivo subido exitosamente a OpenAI. ID del archivo: %s (purpose: %s)", id, purpose));
                    return id;
                }

                String errorMsg = "Error HTTP " + response.code() + " al subir archivo '" + file.getName() +
                        "' (purpose: " + purpose + "). Respuesta: " + responseBodyStr;
                LOGGER.severe(errorMsg);
                throw new OpenAIAPIException(errorMsg);
            }

        } catch (OpenAIAPIException e) {
            throw e;
        } catch (Exception e) {
            String errorMsg = "Error al subir archivo '" + (file != null ? file.getName() : "null") + "' a OpenAI: " + e.getMessage();
            LOGGER.severe(errorMsg);
            throw new OpenAIAPIException(errorMsg, e);
        }
    }

    public void downloadFile(String fileId, String outputFilePath) throws OpenAIAPIException {
        try {
            if (fileId == null || fileId.isBlank()) {
                throw new OpenAIAPIException("fileId vacío");
            }
            if (outputFilePath == null || outputFilePath.isBlank()) {
                throw new OpenAIAPIException("outputFilePath vacío");
            }

            String downloadUrl = url + "/" + fileId + "/content";

            Request request = new Request.Builder()
                    .url(downloadUrl)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String responseBodyStr = response.body() != null ? response.body().string() : "";
                    String errorMsg = "Error HTTP " + response.code() + " al descargar el archivo. Respuesta: " + responseBodyStr;
                    LOGGER.severe(errorMsg);
                    throw new OpenAIAPIException(errorMsg);
                }

                ResponseBody body = response.body();
                if (body == null) {
                    String errorMsg = "Respuesta sin cuerpo al descargar el archivo.";
                    LOGGER.severe(errorMsg);
                    throw new OpenAIAPIException(errorMsg);
                }

                File outputFile = new File(outputFilePath);
                File parent = outputFile.getParentFile();
                if (parent != null) parent.mkdirs();

                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(body.bytes());
                }

                LOGGER.info(() -> String.format("Archivo descargado exitosamente a: %s", outputFilePath));
            }

        } catch (OpenAIAPIException e) {
            throw e;
        } catch (IOException e) {
            String errorMsg = "Error IO al descargar/escribir archivo: " + e.getMessage();
            LOGGER.severe(errorMsg);
            throw new OpenAIAPIException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Error al descargar archivo: " + e.getMessage();
            LOGGER.severe(errorMsg);
            throw new OpenAIAPIException(errorMsg, e);
        }
    }

    /**
     * Cierra el cliente OkHttp y libera sus recursos (connection pool, threads, etc.)
     */
    @Override
    public void close() {
        if (client != null) {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
            if (client.cache() != null) {
                try {
                    client.cache().close();
                } catch (IOException e) {
                    LOGGER.warning("Error al cerrar cache de OkHttp: " + e.getMessage());
                }
            }
        }
    }
}

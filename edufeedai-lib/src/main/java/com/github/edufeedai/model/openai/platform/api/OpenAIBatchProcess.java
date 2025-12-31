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

import com.github.edufeedai.model.openai.platform.api.batches.BatchJob;
import com.github.edufeedai.model.openai.platform.api.helpers.GsonResponseHandler;
import com.github.edufeedai.model.openai.platform.api.exceptions.OpenAIAPIException;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase para gestionar el procesamiento por lotes (batch) de archivos en la API de OpenAI.
 * Permite enviar un archivo para su procesamiento masivo usando la API REST de OpenAI.
 */
public class OpenAIBatchProcess {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIBatchProcess.class);
    /** Clave de API de OpenAI */
    String apiKey;
    /** URL del endpoint batch de OpenAI */
    String batchUrl;
    /** Petición HTTP POST configurada para el batch */
    HttpPost batchRequest;

    /**
     * Crea un proceso batch usando la URL por defecto de la API de OpenAI.
     * @param apiKey Clave de API de OpenAI
     */
    public OpenAIBatchProcess(String apiKey){
        this(apiKey,"https://api.openai.com/v1/batches");
    }

    /**
     * Crea un proceso batch usando una URL personalizada para la API de OpenAI.
     * @param apiKey Clave de API de OpenAI
     * @param batchUrl URL del endpoint batch de OpenAI
     */
    public OpenAIBatchProcess(String apiKey,String batchUrl){
        this.apiKey = apiKey;
        this.batchUrl = batchUrl;
        batchRequest = new HttpPost(batchUrl);
        batchRequest.setHeader("Authorization", "Bearer " + apiKey);
        batchRequest.setHeader("Content-Type", "application/json");
        logger.info("OpenAIBatchProcess inicializado. Endpoint: {}", batchUrl);
    }

    /**
     * Encola un archivo para su procesamiento por lotes en OpenAI.
     *
     * @param fileId ID del archivo previamente subido a OpenAI
     * @return Un objeto BatchJob con la información del proceso encolado
     * @throws OpenAIAPIException Si ocurre un error en la petición o respuesta de la API
     */
    public BatchJob enqueueBatchProcess(String fileId) throws OpenAIAPIException {
        logger.info("Encolando archivo {} para procesamiento batch.", fileId);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Crea el JSON con los parámetros de batch processing
            JSONObject json = new JSONObject();
            json.put("input_file_id", fileId);
            json.put("endpoint", "/v1/chat/completions");
            json.put("completion_window","24h");
            StringEntity requestEntity = new StringEntity(json.toString(), ContentType.APPLICATION_JSON);
            batchRequest.setEntity(requestEntity);
            try (CloseableHttpResponse response = httpClient.execute(batchRequest)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                logger.debug("Respuesta recibida del endpoint batch. Status: {}", statusCode);
                if (statusCode == 200) {
                    logger.info("Procesamiento batch encolado correctamente para archivo {}.", fileId);
                    return GsonResponseHandler.convertJsonToObject(responseBody,BatchJob.class);
                } else {
                    logger.error("Error en respuesta batch: {}", responseBody);
                    throw new OpenAIAPIException(responseBody);
                }
            }
        } catch (Exception e) {
            logger.error("Excepción al encolar procesamiento batch para archivo {}", fileId, e);
            throw new OpenAIAPIException(e);
        }
    }

    public BatchJob getBatchJob(String jobId) throws OpenAIAPIException {
        logger.info("Obteniendo información del job batch con ID: {}", jobId);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(batchUrl + "/" + jobId);
            request.setHeader("Authorization", "Bearer " + apiKey);
            request.setHeader("Content-Type", "application/json");
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                logger.debug("Respuesta recibida al obtener job batch. Status: {}", statusCode);
                if (statusCode == 200) {
                    return GsonResponseHandler.convertJsonToObject(responseBody, BatchJob.class);
                } else {
                    logger.error("Error al obtener job batch: {}", responseBody);
                    throw new OpenAIAPIException(responseBody);
                }
            }
        } catch (Exception e) {
            logger.error("Excepción al obtener job batch con ID {}", jobId, e);
            throw new OpenAIAPIException(e);
        }
    }
}

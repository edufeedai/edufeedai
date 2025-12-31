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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.edufeedai.model.openai.platform.api.exceptions.OpenAIAPIException;
import com.github.edufeedai.model.openai.platform.api.interfaces.CorrectionPromptBuilder;
import com.github.edufeedai.model.openai.platform.api.interfaces.exceptions.APIException;

public class OpenAICorrectionPromptBuilder implements CorrectionPromptBuilder {

    private String messageRoleSystem;
    private String context;
    private String activityStatement;
    private String rubric;
    private String apiKey;

    public OpenAICorrectionPromptBuilder(String context, String activityStatement, String rubric, String apiKey) {
        this.messageRoleSystem = loadSystemConfigFromResources();
        this.context = context;
        this.activityStatement = activityStatement;
        this.rubric = rubric;
        this.apiKey = apiKey;
    }

    private String loadSystemConfigFromResources() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("openaisystem.config")) {
            if (inputStream == null) {
                throw new IllegalStateException("No se pudo encontrar el archivo openaisystem.config en los recursos");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error al leer el archivo openaisystem.config desde los recursos", e);
        }
    }

    protected String buildPrompt() {
        return String.format(
            "[CONTEXTO]\n" +
            "%s\n" +
            "[/CONTEXTO]\n\n" +

            "[ENUNCIADO]\n" +
            "%s\n" +
            "[/ENUNCIADO]\n\n" +

            "[RUBRICA]\n" +
            "%s\n" +
            "[/RUBRICA]",
            context,
            activityStatement,
            rubric
        );
    }

    public String generatePromptCheckString() throws APIException {

        String prompt = buildPrompt();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpClientResponseHandler<String> responseHandler = this::handleResponse;

            HttpPost httpPost = new HttpPost("https://api.openai.com/v1/chat/completions");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);

            // Construir el cuerpo de la solicitud en JSON
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-4o");

            // Mensajes del chat
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", messageRoleSystem);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);

            
            requestBody.put("messages", new JSONArray().put(systemMessage).put(userMessage));

           
            httpPost.setEntity(new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON));

            // Ejecutamos la petición y manejamos la respuesta
            return httpClient.execute(httpPost, responseHandler);

        } catch (RuntimeException e) {
        
            if (e.getCause() instanceof OpenAIAPIException) {
                throw (OpenAIAPIException) e.getCause();
            }
            throw new OpenAIAPIException("Error inesperado al procesar la solicitud", e);
        } catch (IOException e) {
            throw new OpenAIAPIException("Error de I/O al realizar la solicitud a la API", e);
        }
    }

    private String handleResponse(ClassicHttpResponse response) {
        try {

            var entity = response.getEntity();
            String result = EntityUtils.toString(entity);

            if (response.getCode() != 200) {

                throw new OpenAIAPIException("Error en la petición a la API de OpenAI: código de respuesta "
                        + response.getCode() + ", mensaje: " + result);
            }

            JSONObject jsonResponse = new JSONObject(result);

            JSONObject choices = jsonResponse.getJSONArray("choices").getJSONObject(0);
            JSONObject message = choices.getJSONObject("message");
            return message.getString("content").trim();

        } catch (Exception e) {
            
            throw new RuntimeException("Error procesando la respuesta de la API", e);
        }
    }

}

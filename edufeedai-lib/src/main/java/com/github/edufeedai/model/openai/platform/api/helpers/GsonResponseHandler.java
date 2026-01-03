/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model.openai.platform.api.helpers;

import com.google.gson.Gson;
public class GsonResponseHandler {

    public static <T> T convertJsonToObject(String jsonString, Class<T> clazz)  {

        // 2. Usa Gson para convertir el String JSON en un objeto de la clase deseada
        Gson gson = new Gson();
        return gson.fromJson(jsonString, clazz);
    }

    public static <T> String convertObjectToJson(T object) {
        // Crear una instancia de Gson
        Gson gson = new Gson();

        // Convertir el objeto a una cadena JSON
        return gson.toJson(object);
    }

}

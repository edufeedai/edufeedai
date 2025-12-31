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

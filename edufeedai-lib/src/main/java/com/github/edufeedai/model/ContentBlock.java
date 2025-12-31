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

package com.github.edufeedai.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representa un bloque de contenido, que puede ser de tipo texto o imagen.
 * Se utiliza para estructurar información en diferentes formatos dentro del sistema.
 */
public class ContentBlock {

    private static final Logger logger = LoggerFactory.getLogger(ContentBlock.class);
    /**
     * Tipo de bloque: "text" para texto o "image" para imagen.
     */
    private String type;
    /**
     * Contenido del bloque. Puede ser texto plano o una referencia a una imagen.
     */
    private String content;

    /**
     * Crea un nuevo bloque de contenido con el tipo y contenido especificados.
     * @param type Tipo de bloque ("text" o "image")
     * @param content Contenido del bloque
     */
    public ContentBlock(String type, String content) {
        this.type = type;
        this.content = content;
        logger.debug("ContentBlock creado: type={}, content=[{}]", type, content != null ? content.substring(0, Math.min(content.length(), 30)) : "null");
    }

    /**
     * Devuelve una representación en texto del bloque de contenido, incluyendo delimitadores.
     * @return Cadena de texto representando el bloque
     */
    @Override
    public String toString() {
        String repr = getType().toUpperCase() + " START {" + System.lineSeparator() +
                getContent() + System.lineSeparator() +
                "} " + getType().toUpperCase() + " END" + System.lineSeparator();
        logger.trace("Serializando ContentBlock: {}", repr.length() > 80 ? repr.substring(0, 80) + "..." : repr);
        return repr;
    }

    /**
     * Obtiene el tipo de bloque ("text" o "image").
     * @return Tipo de bloque
     */
    public String getType() {
        return type;
    }

    /**
     * Obtiene el contenido del bloque.
     * @return Contenido del bloque
     */
    public String getContent() {
        return content;
    }

    /**
     * Establece el tipo de bloque.
     * @param type Nuevo tipo de bloque ("text" o "image")
     */
    public void setType(String type) {
        logger.debug("Cambiando type de ContentBlock de '{}' a '{}'", this.type, type);
        this.type = type;
    }

    /**
     * Establece el contenido del bloque.
     * @param content Nuevo contenido del bloque
     */
    public void setContent(String content) {
        logger.debug("Cambiando content de ContentBlock. Longitud anterior: {}, nueva: {}", this.content != null ? this.content.length() : 0, content != null ? content.length() : 0);
        this.content = content;
    }

}

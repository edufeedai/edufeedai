/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.cli.commands;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Interfaz base para todos los comandos CLI.
 */
public interface Command {

    /**
     * Ejecuta el comando con los argumentos proporcionados.
     *
     * @param args Argumentos del comando (sin incluir el nombre del comando)
     * @throws IOException Si ocurre un error de I/O
     * @throws SQLException Si ocurre un error de base de datos
     */
    void execute(String[] args) throws IOException, SQLException;

    /**
     * Obtiene el nombre del comando.
     *
     * @return Nombre del comando
     */
    String getName();

    /**
     * Obtiene la descripción del comando para la ayuda.
     *
     * @return Descripción del comando
     */
    String getDescription();
}

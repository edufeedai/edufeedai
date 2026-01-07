/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.cli.commands;

import com.github.edufeedai.cli.config.AppConfig;
import com.github.edufeedai.cli.utils.DatabaseUtils;
import com.github.edufeedai.cli.utils.UIUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

/**
 * Comando para inicializar el workspace y crear la base de datos.
 */
public class InitCommand implements Command {

    @Override
    public void execute(String[] args) throws IOException, SQLException {
        // Determinar directorio de trabajo
        Path workDirPath;
        if (args.length > 0 && !args[0].isEmpty()) {
            workDirPath = Paths.get(args[0]).toAbsolutePath();
        } else {
            String envWorkDir = AppConfig.getWorkingDirectory();
            workDirPath = Paths.get(envWorkDir).toAbsolutePath();
        }

        UIUtils.printTitle("Inicializando workspace");
        System.out.println("Directorio de trabajo: " + workDirPath);

        // Crear directorio si no existe
        if (!Files.exists(workDirPath)) {
            System.out.println("Creando directorio de trabajo...");
            Files.createDirectories(workDirPath);
        }

        // Crear base de datos SQLite
        DatabaseUtils.createDatabase(workDirPath);

        System.out.println();
        UIUtils.printSeparator();
        UIUtils.printSuccess("Workspace inicializado exitosamente");
        System.out.println("Directorio de trabajo: " + workDirPath);
        System.out.println("Base de datos: " + workDirPath.resolve(AppConfig.getConfigFolder()).resolve("edufeedai.db"));
        System.out.println("\nPróximos pasos:");
        System.out.println("  - Usa 'add <archivo.zip>' para añadir entregas");
    }

    @Override
    public String getName() {
        return "init";
    }

    @Override
    public String getDescription() {
        return "Inicializa el workspace y crea la base de datos";
    }
}

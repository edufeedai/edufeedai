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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.github.edufeedai.model.FileProcessor;
import com.github.edufeedai.model.openai.platform.api.OpenAIBatchProcess;
import com.github.edufeedai.model.openai.platform.api.OpenAICorrectionPromptBuilder;
import com.github.edufeedai.model.openai.platform.api.OpenAIFileManagement;
import com.github.edufeedai.model.openai.platform.api.batches.BatchJob;
import com.github.edufeedai.model.openai.platform.api.exceptions.OpenAIAPIException;

import io.github.cdimascio.dotenv.Dotenv;

public class App {
    // Configuración
    private static final String CONFIG_FOLDER = ".edufeedai";
    private static final Dotenv dotenv = loadDotenv();

    /**
     * Carga el archivo .env si existe, si no, devuelve una instancia que lee del sistema
     */
    private static Dotenv loadDotenv() {
        try {
            return Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
        } catch (Exception e) {
            // Si falla, usar configuración del sistema
            return Dotenv.configure()
                    .ignoreIfMissing()
                    .systemProperties()
                    .load();
        }
    }

    /**
     * Obtiene el directorio de trabajo desde .env o usa el directorio actual
     */
    private static String getWorkingDirectory() {
        String workDir = dotenv.get("WORK_DIR");
        if (workDir == null || workDir.isEmpty()) {
            return ".";
        }
        return workDir;
    }

    /**
     * Obtiene la ruta completa del archivo de base de datos en el directorio de trabajo
     */
    private static String getDatabasePath() {
        String workDir = getWorkingDirectory();
        return Paths.get(workDir).resolve(CONFIG_FOLDER).resolve("edufeedai.db").toString();
    }

    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("help") || args[0].equals("--help")) {
            printHelp();
            return;
        }
        try {
            switch (args[0]) {
                case "init":
                    String workDir = (args.length >= 2) ? args[1] : null;
                    initCommand(workDir);
                    break;
                case "add":
                    if (args.length < 2) {
                        System.out.println("Uso: add <archivo.zip>");
                        return;
                    }
                    addCommand(args[1]);
                    break;
                case "grading":
                    Integer taskNumberForGrading = null;
                    if (args.length >= 2) {
                        try {
                            taskNumberForGrading = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            System.err.println("Error: El número de tarea debe ser un número entero.");
                            System.err.println("Usa 'grading' sin argumentos para ver la lista de tareas.");
                            return;
                        }
                    }
                    gradingCommand(taskNumberForGrading);
                    break;
                case "process":
                    Integer taskNumber = null;
                    if (args.length >= 2) {
                        try {
                            taskNumber = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            System.err.println("Error: El número de tarea debe ser un número entero.");
                            System.err.println("Usa 'process' sin argumentos para ver la lista de tareas.");
                            return;
                        }
                    }
                    processCommand(taskNumber);
                    break;
                case "check":
                    Integer taskNumberForCheck = null;
                    if (args.length >= 2) {
                        try {
                            taskNumberForCheck = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            System.err.println("Error: El número de tarea debe ser un número entero.");
                            System.err.println("Usa 'check' sin argumentos para ver el batch más reciente.");
                            return;
                        }
                    }
                    checkCommand(taskNumberForCheck);
                    break;
                case "download":
                    Integer taskNumberForDownload = null;
                    if (args.length >= 2) {
                        try {
                            taskNumberForDownload = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            System.err.println("Error: El número de tarea debe ser un número entero.");
                            System.err.println("Usa 'download' sin argumentos para descargar el batch más reciente.");
                            return;
                        }
                    }
                    downloadCommand(taskNumberForDownload);
                    break;
                case "package":
                    Integer taskNumberForPackage = null;
                    if (args.length >= 2) {
                        try {
                            taskNumberForPackage = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            System.err.println("Error: El número de tarea debe ser un número entero.");
                            System.err.println("Usa 'package' sin argumentos para empaquetar el batch más reciente.");
                            return;
                        }
                    }
                    packageCommand(taskNumberForPackage);
                    break;
                case "status":
                    Integer taskNumberForStatus = null;
                    if (args.length >= 2) {
                        try {
                            taskNumberForStatus = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            System.err.println("Error: El número de tarea debe ser un número entero.");
                            System.err.println("Usa 'status' sin argumentos para ver todas las tareas.");
                            return;
                        }
                    }
                    statusCommand(taskNumberForStatus);
                    break;
                default:
                    System.out.println("Comando desconocido: " + args[0]);
                    printHelp();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printHelp() {
        System.out.println("EduFeedAI CLI - Uso:");
        System.out.println("  init [directorio]    Inicializa el workspace y crea la base de datos");
        System.out.println("                       Sin directorio usa WORK_DIR del .env, o '.' si no existe");
        System.out.println("  add <archivo.zip>    Añade entregas desde un ZIP (nombre del ZIP = nombre de la tarea)");
        System.out.println("  grading              Lista todas las tareas y su estado de configuración de rúbrica");
        System.out.println("  grading <número>     Configura o muestra la rúbrica de una tarea específica");
        System.out.println("  process              Lista todas las tareas con su estado");
        System.out.println("  process <número>     Procesa una tarea específica por su número y la envía a OpenAI");
        System.out.println("  check                Consulta el estado del batch más reciente en OpenAI");
        System.out.println("  check <número>       Consulta el estado del batch de una tarea específica");
        System.out.println("  download             Descarga los resultados del batch más reciente");
        System.out.println("  download <número>    Descarga los resultados del batch de una tarea específica");
        System.out.println("  package              Genera el ZIP de feedback con el batch más reciente");
        System.out.println("  package <número>     Genera el ZIP de feedback de una tarea específica");
        System.out.println("  status               Muestra todas las tareas y el estado de sus entregas");
        System.out.println("  status <número>      Muestra detalles y estadísticas de una tarea específica");
        System.out.println("  help                 Muestra esta ayuda");
    }

    // Comando: init [directorio]
    private static void initCommand(String workDir) throws IOException, SQLException {
        // Determinar directorio de trabajo
        Path workDirPath;
        if (workDir != null && !workDir.isEmpty()) {
            workDirPath = Paths.get(workDir).toAbsolutePath();
        } else {
            String envWorkDir = dotenv.get("WORK_DIR");
            if (envWorkDir != null && !envWorkDir.isEmpty()) {
                workDirPath = Paths.get(envWorkDir).toAbsolutePath();
            } else {
                workDirPath = Paths.get(".").toAbsolutePath();
            }
        }

        System.out.println("=== Inicializando workspace ===");
        System.out.println("Directorio de trabajo: " + workDirPath);

        // Crear directorio si no existe
        if (!Files.exists(workDirPath)) {
            System.out.println("Creando directorio de trabajo...");
            Files.createDirectories(workDirPath);
        }

        // Crear base de datos SQLite
        createDatabase(workDirPath);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("✓ Workspace inicializado exitosamente");
        System.out.println("Directorio de trabajo: " + workDirPath);
        System.out.println("Base de datos: " + workDirPath.resolve(CONFIG_FOLDER).resolve("edufeedai.db"));
        System.out.println("\nPróximos pasos:");
        System.out.println("  - Usa 'add <archivo.zip>' para añadir entregas");
    }

    // Comando: add <zip>
    private static void addCommand(String zipPath) throws IOException, SQLException {
        String workDir = getWorkingDirectory();
        Path workDirPath = Paths.get(workDir).toAbsolutePath();

        // Verificar que el workspace está inicializado
        File dbFile = new File(getDatabasePath());
        if (!dbFile.exists()) {
            System.err.println("Error: Workspace no inicializado.");
            System.err.println("Ejecuta 'init' primero para crear el workspace.");
            return;
        }

        // Obtener nombre de la tarea del archivo ZIP (sin extensión)
        File zipFile = new File(zipPath);
        if (!zipFile.exists()) {
            System.err.println("Error: Archivo ZIP no encontrado: " + zipPath);
            return;
        }

        String zipFileName = zipFile.getName();
        String taskName = zipFileName.replaceFirst("[.][^.]+$", ""); // Quitar extensión
        String moodleTaskId = extractMoodleTaskId(zipFileName);

        System.out.println("=== Añadiendo entregas ===");
        System.out.println("Archivo ZIP: " + zipFileName);
        System.out.println("Nombre de la tarea: " + taskName);
        if (moodleTaskId != null) {
            System.out.println("ID de Moodle: " + moodleTaskId);
        }

        // Verificar si la tarea ya existe en la BD
        long now = System.currentTimeMillis() / 1000;
        int taskId;
        boolean taskExists = false;

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath())) {
            // Verificar si existe
            PreparedStatement checkStmt = conn.prepareStatement("SELECT id FROM tasks WHERE name = ?");
            checkStmt.setString(1, taskName);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                taskExists = true;
                taskId = rs.getInt("id");

                // Contar entregas existentes
                PreparedStatement countStmt = conn.prepareStatement(
                    "SELECT COUNT(*) as count FROM submissions WHERE task_id = ?"
                );
                countStmt.setInt(1, taskId);
                ResultSet countRs = countStmt.executeQuery();
                int count = countRs.getInt("count");

                System.out.println("\n⚠ ADVERTENCIA: La tarea '" + taskName + "' ya existe con " + count + " entregas.");
                System.out.println("Se eliminarán todas las entregas existentes y sus archivos (CASCADE).");
                System.out.print("¿Continuar? (s/N): ");

                Scanner scanner = new Scanner(System.in);
                String response = scanner.nextLine().trim().toLowerCase();
                if (!response.equals("s") && !response.equals("si")) {
                    System.out.println("Operación cancelada.");
                    return;
                }

                // Eliminar entregas anteriores (CASCADE eliminará submission_files automáticamente)
                PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM submissions WHERE task_id = ?");
                deleteStmt.setInt(1, taskId);
                int deleted = deleteStmt.executeUpdate();
                System.out.println("✓ Eliminadas " + deleted + " entregas anteriores.");

                // Actualizar moodle_task_id si está disponible
                if (moodleTaskId != null) {
                    PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE tasks SET moodle_task_id = ?, updated_at = ? WHERE id = ?"
                    );
                    updateStmt.setString(1, moodleTaskId);
                    updateStmt.setLong(2, now);
                    updateStmt.setInt(3, taskId);
                    updateStmt.executeUpdate();
                }

            } else {
                // Crear nueva tarea
                PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO tasks (name, moodle_task_id, created_at) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                insertStmt.setString(1, taskName);
                insertStmt.setString(2, moodleTaskId);
                insertStmt.setLong(3, now);
                insertStmt.executeUpdate();

                ResultSet genKeys = insertStmt.getGeneratedKeys();
                if (genKeys.next()) {
                    taskId = genKeys.getInt(1);
                } else {
                    throw new SQLException("No se pudo crear la tarea");
                }
            }
        }

        // Crear directorio de la tarea
        Path taskDir = workDirPath.resolve(taskName);
        if (Files.exists(taskDir)) {
            // Eliminar contenido anterior
            deleteDirectory(taskDir);
        }
        Files.createDirectories(taskDir);

        // Descomprimir ZIP en directorio temporal
        Path tempDir = Files.createTempDirectory("edufeedai_extract_");
        try {
            unzip(zipPath, tempDir);

            // Cada subdirectorio es una entrega de estudiante
            File[] studentDirs = tempDir.toFile().listFiles(File::isDirectory);
            if (studentDirs == null || studentDirs.length == 0) {
                System.err.println("Advertencia: No se encontraron directorios de estudiantes en el ZIP.");
                return;
            }

            System.out.println("\nEncontrados " + studentDirs.length + " estudiantes.");

            for (File studentDir : studentDirs) {
                String studentName = studentDir.getName();
                System.out.println("Procesando: " + studentName);

                // Crear entrega en la BD
                int submissionId;
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath())) {
                    PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO submissions (task_id, student_name, submission_number, submitted_at, created_at) " +
                        "VALUES (?, ?, 1, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                    );
                    stmt.setInt(1, taskId);
                    stmt.setString(2, studentName);
                    stmt.setLong(3, now);
                    stmt.setLong(4, now);
                    stmt.executeUpdate();

                    ResultSet genKeys = stmt.getGeneratedKeys();
                    if (genKeys.next()) {
                        submissionId = genKeys.getInt(1);
                    } else {
                        throw new SQLException("No se pudo crear la entrega");
                    }
                }

                // Copiar archivos del estudiante a su directorio
                Path submissionDir = taskDir.resolve(studentName);
                copyDirectory(studentDir.toPath(), submissionDir);

                System.out.println("  ✓ " + submissionDir);
            }

            System.out.println("\n" + "=".repeat(60));
            System.out.println("✓ Entregas añadidas exitosamente");
            System.out.println("Tarea: " + taskName + " (ID: " + taskId + ")");
            System.out.println("Total entregas: " + studentDirs.length);
            System.out.println("Ubicación: " + taskDir);

        } finally {
            deleteDirectory(tempDir);
        }
    }

    // Método auxiliar: copiar directorio recursivamente
    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(sourcePath -> {
            try {
                Path targetPath = target.resolve(source.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    if (!Files.exists(targetPath)) {
                        Files.createDirectories(targetPath);
                    }
                } else {
                    Files.copy(sourcePath, targetPath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Método auxiliar: eliminar directorio recursivamente
    private static void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                .sorted((a, b) -> -a.compareTo(b)) // Ordenar en reversa para borrar archivos antes que directorios
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignorar errores al eliminar archivos temporales
                    }
                });
        }
    }

    // Comando: grading [numero_tarea]
    private static void gradingCommand(Integer taskNumber) throws SQLException, IOException {
        if (taskNumber == null) {
            // Modo lista: mostrar todas las tareas con su estado de configuración
            listTasksWithGradingStatus();
        } else {
            // Modo configurar: configurar o mostrar configuración de tarea específica
            configureOrShowGradingForTask(taskNumber);
        }
    }

    // Lista todas las tareas con su estado de configuración de rúbrica
    private static void listTasksWithGradingStatus() throws SQLException {
        System.out.println("=== Estado de Configuración de Rúbricas ===\n");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath())) {
            // Obtener todas las tareas con su estado de configuración
            Statement stmt = conn.createStatement();
            ResultSet tasksRs = stmt.executeQuery(
                "SELECT t.id, t.name, t.grading_config_id, " +
                "COUNT(s.id) as total_submissions " +
                "FROM tasks t " +
                "LEFT JOIN submissions s ON t.id = s.task_id " +
                "GROUP BY t.id, t.name, t.grading_config_id " +
                "ORDER BY t.created_at DESC"
            );

            int taskCount = 0;
            while (tasksRs.next()) {
                taskCount++;
                int taskId = tasksRs.getInt("id");
                String name = tasksRs.getString("name");
                Integer gradingConfigId = tasksRs.getObject("grading_config_id") != null
                    ? tasksRs.getInt("grading_config_id") : null;
                int totalSubmissions = tasksRs.getInt("total_submissions");

                // Marcar tareas con/sin configuración
                String marker = (gradingConfigId != null) ? " ✓" : " ⚠";
                String status = (gradingConfigId != null) ? "Configurada" : "Sin configurar";

                System.out.printf("%d. %-30s %-15s %d entregas%s%n",
                    taskCount, name, status, totalSubmissions, marker);
            }

            if (taskCount == 0) {
                System.out.println("No hay tareas registradas.");
                System.out.println("\nUsa 'add <archivo.zip>' para añadir entregas.");
            } else {
                System.out.println("\nTotal: " + taskCount + " tareas");
                System.out.println("\nLeyenda:");
                System.out.println("  ⚠ = Rúbrica no configurada");
                System.out.println("  ✓ = Rúbrica configurada");
                System.out.println("\nPara configurar/ver rúbrica: grading <número_tarea>");
            }
        }
    }

    // Configura o muestra la configuración de una tarea específica
    private static void configureOrShowGradingForTask(Integer taskNumber) throws SQLException, IOException {
        // 1. Verificar API key
        String apiKey = dotenv.get("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Error: La variable de entorno OPENAI_API_KEY no está configurada.");
            System.err.println("Por favor, configura tu API key de OpenAI en el archivo .env o como variable de entorno del sistema.");
            return;
        }

        // 2. Verificar que la base de datos existe
        File dbFile = new File(getDatabasePath());
        if (!dbFile.exists()) {
            System.err.println("Error: La base de datos no existe.");
            System.err.println("Ejecuta 'init' primero para inicializar el entorno.");
            return;
        }

        // 3. Obtener la tarea por número de lista
        int taskId = -1;
        String taskName = null;
        Integer existingGradingConfigId = null;
        boolean found = false;

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath())) {
            Statement stmt = conn.createStatement();
            ResultSet tasksRs = stmt.executeQuery(
                "SELECT t.id, t.name, t.grading_config_id " +
                "FROM tasks t " +
                "ORDER BY t.created_at DESC"
            );

            int currentNumber = 0;
            while (tasksRs.next()) {
                currentNumber++;
                if (currentNumber == taskNumber) {
                    taskId = tasksRs.getInt("id");
                    taskName = tasksRs.getString("name");
                    existingGradingConfigId = tasksRs.getObject("grading_config_id") != null
                        ? tasksRs.getInt("grading_config_id") : null;
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            System.err.println("Error: No existe la tarea número " + taskNumber);
            System.err.println("Usa 'grading' sin argumentos para ver las tareas disponibles.");
            return;
        }

        System.out.println("=== Configuración de Rúbrica para Tarea #" + taskNumber + " ===");
        System.out.println("Tarea: " + taskName);
        System.out.println("ID: " + taskId + "\n");

        // 4. Si ya tiene configuración, mostrarla
        if (existingGradingConfigId != null) {
            showExistingGradingConfig(existingGradingConfigId);
            return;
        }

        // 5. Si no tiene configuración, crearla
        System.out.println("⚠ Esta tarea no tiene rúbrica configurada.\n");
        createGradingConfigForTask(taskId, taskName, apiKey);
    }

    // Muestra la configuración de rúbrica existente
    private static void showExistingGradingConfig(int gradingConfigId) throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath())) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT message_role_system, context, activity_statement, rubric, generated_instructions, created_at " +
                "FROM grading_config WHERE id = ?"
            );
            stmt.setInt(1, gradingConfigId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("✓ Rúbrica configurada\n");
                System.out.println("Fecha de creación: " + formatTimestamp(rs.getLong("created_at")));
                System.out.println("\n" + "=".repeat(60));
                System.out.println("MENSAJE ROL DEL SISTEMA:");
                System.out.println("-".repeat(60));
                System.out.println(rs.getString("message_role_system"));

                System.out.println("\n" + "=".repeat(60));
                System.out.println("CONTEXTO:");
                System.out.println("-".repeat(60));
                System.out.println(rs.getString("context"));

                System.out.println("\n" + "=".repeat(60));
                System.out.println("ENUNCIADO:");
                System.out.println("-".repeat(60));
                System.out.println(rs.getString("activity_statement"));

                System.out.println("\n" + "=".repeat(60));
                System.out.println("RÚBRICA:");
                System.out.println("-".repeat(60));
                System.out.println(rs.getString("rubric"));

                System.out.println("\n" + "=".repeat(60));
                System.out.println("INSTRUCCIONES GENERADAS:");
                System.out.println("-".repeat(60));
                System.out.println(rs.getString("generated_instructions"));
                System.out.println("=".repeat(60));
            }
        }
    }

    // Crea una nueva configuración de rúbrica para una tarea
    private static void createGradingConfigForTask(int taskId, String taskName, String apiKey) throws SQLException, IOException {
        Scanner scanner = new Scanner(System.in);

        try {
            // Solicitar información al usuario
            System.out.println("Introduce la información necesaria para generar las instrucciones de corrección.\n");
            System.out.println("Nota: El mensaje del rol del sistema se cargará automáticamente desde la configuración.\n");

            System.out.println("1. Contexto de la actividad:");
            System.out.println("   (Ejemplo: 'Es un segundo curso de un ciclo formativo de grado superior de ASIX...')");
            System.out.print("   > ");
            String context = scanner.nextLine().trim();

            System.out.println("\n2. Enunciado de la actividad:");
            System.out.println("   (Ejemplo: 'Se pide realizar una configuración DNS en linux utilizando bind 9...')");
            System.out.print("   > ");
            String activityStatement = scanner.nextLine().trim();

            System.out.println("\n3. Rúbrica de corrección:");
            System.out.println("   (Ejemplo: 'Ha creado los ficheros de zona: db.asixalcoy.org y db.192.168.0...')");
            System.out.print("   > ");
            String rubric = scanner.nextLine().trim();

            // Validar que no haya campos vacíos
            if (context.isEmpty() || activityStatement.isEmpty() || rubric.isEmpty()) {
                System.err.println("\nError: Todos los campos son obligatorios.");
                return;
            }

            // Generar instrucciones usando OpenAICorrectionPromptBuilder
            System.out.println("\n" + "=".repeat(60));
            System.out.println("Generando instrucciones de corrección con OpenAI...");
            System.out.println("Esto puede tardar unos segundos...\n");

            try {
                OpenAICorrectionPromptBuilder builder = new OpenAICorrectionPromptBuilder(
                    context,
                    activityStatement,
                    rubric,
                    apiKey
                );

                String generatedInstructions = builder.generatePromptCheckString();

                // Obtener el messageRoleSystem que se cargó automáticamente desde los recursos
                // para guardarlo en la base de datos como registro
                String messageRoleSystem = loadSystemConfigFromResources();

                System.out.println("✓ Instrucciones generadas exitosamente:\n");
                System.out.println("-".repeat(60));
                System.out.println(generatedInstructions);
                System.out.println("-".repeat(60));

                // Guardar en la base de datos y vincular con la tarea
                System.out.println("\nGuardando configuración en la base de datos...");
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath())) {
                    // Insertar la configuración
                    PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO grading_config (message_role_system, context, activity_statement, rubric, generated_instructions, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                    );

                    insertStmt.setString(1, messageRoleSystem);
                    insertStmt.setString(2, context);
                    insertStmt.setString(3, activityStatement);
                    insertStmt.setString(4, rubric);
                    insertStmt.setString(5, generatedInstructions);
                    insertStmt.setLong(6, System.currentTimeMillis() / 1000);
                    insertStmt.executeUpdate();

                    ResultSet genKeys = insertStmt.getGeneratedKeys();
                    if (genKeys.next()) {
                        int gradingConfigId = genKeys.getInt(1);

                        // Actualizar la tarea para vincularla con esta configuración
                        PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE tasks SET grading_config_id = ?, updated_at = ? WHERE id = ?"
                        );
                        updateStmt.setInt(1, gradingConfigId);
                        updateStmt.setLong(2, System.currentTimeMillis() / 1000);
                        updateStmt.setInt(3, taskId);
                        updateStmt.executeUpdate();

                        System.out.println("✓ Configuración guardada y vinculada a la tarea.");
                    }
                }

                // Mostrar resumen
                System.out.println("\n" + "=".repeat(60));
                System.out.println("✓ Configuración de corrección completada exitosamente");
                System.out.println("Tarea: " + taskName);
                System.out.println("\nPróximos pasos:");
                System.out.println("  - Usa 'process' para ver las tareas disponibles");
                System.out.println("  - Esta rúbrica se usará automáticamente al procesar esta tarea");

            } catch (Exception e) {
                System.err.println("Error al generar instrucciones con OpenAI: " + e.getMessage());
                throw new IOException("Error en API de OpenAI", e);
            }

        } finally {
            scanner.close();
        }
    }

    // Comando: process [numero_tarea]
    private static void processCommand(Integer taskNumber) throws SQLException, IOException {
        if (taskNumber == null) {
            // Modo lista: mostrar todas las tareas con su estado
            listTasksWithStatus();
        } else {
            // Modo procesar: procesar la tarea específica por número
            processTask(taskNumber);
        }
    }

    // Lista todas las tareas con su estado
    private static void listTasksWithStatus() throws SQLException {
        System.out.println("=== Tareas Disponibles ===\n");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath())) {
            // Obtener todas las tareas con estadísticas
            Statement stmt = conn.createStatement();
            ResultSet tasksRs = stmt.executeQuery(
                "SELECT t.id, t.name, " +
                "COUNT(s.id) as total, " +
                "SUM(CASE WHEN s.status = 'pending' THEN 1 ELSE 0 END) as pending, " +
                "SUM(CASE WHEN s.status != 'pending' THEN 1 ELSE 0 END) as completed " +
                "FROM tasks t " +
                "LEFT JOIN submissions s ON t.id = s.task_id " +
                "GROUP BY t.id, t.name " +
                "ORDER BY t.created_at DESC"
            );

            int taskCount = 0;
            while (tasksRs.next()) {
                taskCount++;
                String name = tasksRs.getString("name");
                int total = tasksRs.getInt("total");
                int pending = tasksRs.getInt("pending");
                int completed = tasksRs.getInt("completed");

                // Crear barra de progreso
                String progressBar = createProgressBar(completed, total);

                // Marcar tareas con entregas pendientes
                String marker = (pending > 0) ? " ⚠" : " ✓";

                System.out.printf("%d. %-30s %s %d/%d procesadas%s%n",
                    taskCount, name, progressBar, completed, total, marker);
            }

            if (taskCount == 0) {
                System.out.println("No hay tareas registradas.");
                System.out.println("\nUsa 'add <archivo.zip>' para añadir entregas.");
            } else {
                System.out.println("\nTotal: " + taskCount + " tareas");
                System.out.println("\nLeyenda:");
                System.out.println("  ⚠ = Tiene entregas pendientes");
                System.out.println("  ✓ = Todas las entregas procesadas");
                System.out.println("\nPara procesar una tarea: process <número_tarea>");
            }
        }
    }

    // Crea una barra de progreso visual
    private static String createProgressBar(int completed, int total) {
        int barLength = 10;
        if (total == 0) return "[" + " ".repeat(barLength) + "]";

        int filled = (int) ((double) completed / total * barLength);
        String bar = "●".repeat(filled) + "○".repeat(barLength - filled);
        return "[" + bar + "]";
    }

    // Procesa una tarea específica por número de lista
    private static void processTask(Integer taskNumber) throws SQLException, IOException {
        // 1. Verificar API key
        String apiKey = dotenv.get("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Error: La variable de entorno OPENAI_API_KEY no está configurada.");
            return;
        }

        // 2. Obtener la tarea por número de lista (ordenada por created_at DESC)
        int taskId = -1;
        String taskName = null;
        String moodleTaskId = null;
        int totalSubmissions = 0;
        Integer gradingConfigId = null;
        boolean found = false;

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath())) {
            // Obtener todas las tareas ordenadas igual que en listTasksWithStatus
            Statement stmt = conn.createStatement();
            ResultSet tasksRs = stmt.executeQuery(
                "SELECT t.id, t.name, t.moodle_task_id, t.grading_config_id, " +
                "COUNT(s.id) as total " +
                "FROM tasks t " +
                "LEFT JOIN submissions s ON t.id = s.task_id " +
                "GROUP BY t.id, t.name, t.moodle_task_id, t.grading_config_id " +
                "ORDER BY t.created_at DESC"
            );

            // Avanzar hasta el número de tarea especificado
            int currentNumber = 0;
            while (tasksRs.next()) {
                currentNumber++;
                if (currentNumber == taskNumber) {
                    taskId = tasksRs.getInt("id");
                    taskName = tasksRs.getString("name");
                    moodleTaskId = tasksRs.getString("moodle_task_id");
                    totalSubmissions = tasksRs.getInt("total");
                    gradingConfigId = tasksRs.getObject("grading_config_id") != null
                        ? tasksRs.getInt("grading_config_id") : null;
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            System.err.println("Error: No existe la tarea número " + taskNumber);
            System.err.println("Usa 'process' sin argumentos para ver las tareas disponibles.");
            return;
        }

        System.out.println("=== Procesando tarea #" + taskNumber + ": " + taskName + " ===\n");
        System.out.println("Tarea ID: " + taskId);
        System.out.println("Total de entregas: " + totalSubmissions);

        if (totalSubmissions == 0) {
            System.out.println("No hay entregas para procesar en esta tarea.");
            return;
        }

        // 3. Verificar que la tarea tenga configuración de rúbrica
        if (gradingConfigId == null) {
            System.err.println("\n⚠ Error: Esta tarea no tiene rúbrica configurada.");
            System.err.println("Ejecuta 'grading " + taskNumber + "' para configurar la rúbrica de esta tarea.");
            return;
        }

        // 4. Obtener instrucciones de corrección específicas de esta tarea
        String instructions;
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath())) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT generated_instructions FROM grading_config WHERE id = ?"
            );
            stmt.setInt(1, gradingConfigId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                System.err.println("Error: No se encontró la configuración de rúbrica para esta tarea.");
                System.err.println("Ejecuta 'grading " + taskNumber + "' para configurar la rúbrica.");
                return;
            }
            instructions = rs.getString("generated_instructions");
            System.out.println("✓ Instrucciones de corrección cargadas para esta tarea.\n");
        }

        // 5. Procesar archivos de las entregas
        System.out.println("=== Fase 1: Escaneo y procesamiento de archivos ===\n");

        String workDir = getWorkingDirectory();
        Path taskDir = Paths.get(workDir).resolve(taskName);

        if (!Files.exists(taskDir)) {
            System.err.println("Error: No existe el directorio de la tarea: " + taskDir);
            return;
        }

        FileProcessor fileProcessor = new FileProcessor();

        int totalFilesProcessed = 0;
        int totalTextFiles = 0;
        int totalPdfFiles = 0;
        int totalUnsupportedFiles = 0;
        String jsonlFilePath;

        // Procesar archivos y extraer contenido (sin subir a OpenAI)
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath())) {
            // Obtener todas las entregas de esta tarea
            PreparedStatement submissionsStmt = conn.prepareStatement(
                "SELECT id, student_name FROM submissions WHERE task_id = ? ORDER BY student_name"
            );
            submissionsStmt.setInt(1, taskId);
            ResultSet submissionsRs = submissionsStmt.executeQuery();

            while (submissionsRs.next()) {
                int submissionId = submissionsRs.getInt("id");
                String studentName = submissionsRs.getString("student_name");

                Path studentDir = taskDir.resolve(studentName);

                if (!Files.exists(studentDir) || !Files.isDirectory(studentDir)) {
                    System.out.println("⚠ Advertencia: No existe directorio para " + studentName);
                    continue;
                }

                System.out.println("Procesando: " + studentName);

                // Obtener todos los archivos del estudiante
                List<File> files = fileProcessor.getAllFiles(studentDir.toFile());

                for (File file : files) {
                    try {
                        // Procesar el archivo y detectar su tipo
                        FileProcessor.FileProcessingResult result = fileProcessor.processFile(file);
                        totalFilesProcessed++;

                        String relativeFilePath = taskDir.relativize(file.toPath()).toString();
                        String contentExtracted = null;
                        int isTextFile = 0;

                        // Extraer contenido según el tipo de archivo
                        switch (result.getProcessingType()) {
                            case TEXT_PLAIN:
                                // Archivo de texto: usar contenido extraído
                                contentExtracted = result.getExtractedText();
                                isTextFile = 1;
                                totalTextFiles++;
                                System.out.println("  ✓ " + file.getName() + " (texto plano, " +
                                    contentExtracted.length() + " caracteres)");
                                break;

                            case PDF_ORIGINAL:
                                // PDF: extraer texto usando OCRMyPDF
                                try {
                                    FileProcessor.FileProcessingResult pdfResult =
                                        fileProcessor.extractTextFromPdf(file, moodleTaskId, studentName, workDir);
                                    contentExtracted = pdfResult.getExtractedText();
                                    totalPdfFiles++;
                                    System.out.println("  ✓ " + file.getName() + " (PDF con OCR, " +
                                        contentExtracted.length() + " caracteres)");
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    System.err.println("  ✗ Proceso OCR interrumpido para " + file.getName());
                                    contentExtracted = "[Proceso OCR interrumpido]";
                                } catch (Exception e) {
                                    System.err.println("  ✗ Error extrayendo texto de PDF " +
                                        file.getName() + ": " + e.getMessage());
                                    contentExtracted = "[Error al extraer texto del PDF]";
                                }
                                break;

                            case UNSUPPORTED:
                                totalUnsupportedFiles++;
                                System.out.println("  ⚠ " + file.getName() + " (tipo no soportado: " +
                                    result.getMimeType() + ")");
                                break;

                            default:
                                System.out.println("  ? " + file.getName() + " (tipo desconocido)");
                                break;
                        }

                        // Verificar si el archivo ya existe en la base de datos
                        PreparedStatement checkFileStmt = conn.prepareStatement(
                            "SELECT id FROM submission_files WHERE submission_id = ? AND file_path = ?"
                        );
                        checkFileStmt.setInt(1, submissionId);
                        checkFileStmt.setString(2, relativeFilePath);
                        ResultSet existingFileRs = checkFileStmt.executeQuery();

                        boolean fileExists = existingFileRs.next();

                        // Guardar o actualizar información del archivo en la base de datos
                        if (fileExists) {
                            // Actualizar registro existente
                            PreparedStatement updateFileStmt = conn.prepareStatement(
                                "UPDATE submission_files SET file_type = ?, file_size = ?, " +
                                "is_text_file = ?, content_extracted = ?, updated_at = ? " +
                                "WHERE submission_id = ? AND file_path = ?"
                            );
                            updateFileStmt.setString(1, result.getMimeType());
                            updateFileStmt.setLong(2, file.length());
                            updateFileStmt.setInt(3, isTextFile);
                            updateFileStmt.setString(4, contentExtracted);
                            updateFileStmt.setLong(5, System.currentTimeMillis() / 1000);
                            updateFileStmt.setInt(6, submissionId);
                            updateFileStmt.setString(7, relativeFilePath);
                            updateFileStmt.executeUpdate();
                        } else {
                            // Insertar nuevo registro (sin openai_file_id)
                            PreparedStatement insertFileStmt = conn.prepareStatement(
                                "INSERT INTO submission_files (submission_id, file_path, file_name, file_type, " +
                                "file_size, is_text_file, content_extracted, created_at) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                            );
                            insertFileStmt.setInt(1, submissionId);
                            insertFileStmt.setString(2, relativeFilePath);
                            insertFileStmt.setString(3, file.getName());
                            insertFileStmt.setString(4, result.getMimeType());
                            insertFileStmt.setLong(5, file.length());
                            insertFileStmt.setInt(6, isTextFile);
                            insertFileStmt.setString(7, contentExtracted);
                            insertFileStmt.setLong(8, System.currentTimeMillis() / 1000);
                            insertFileStmt.executeUpdate();
                        }

                    } catch (Exception e) {
                        System.err.println("  ✗ Error procesando " + file.getName() + ": " + e.getMessage());
                    }
                }
            }

            System.out.println("\n" + "=".repeat(60));
            System.out.println("✓ Extracción de contenido completada");
            System.out.println("Total archivos procesados: " + totalFilesProcessed);
            System.out.println("  - Archivos de texto: " + totalTextFiles);
            System.out.println("  - Archivos PDF: " + totalPdfFiles);
            System.out.println("  - Archivos no soportados: " + totalUnsupportedFiles);

            // Generar submission_id_map.json para mapeo de entregas (debe ir ANTES del JSONL)
            System.out.println("\n" + "=".repeat(60));
            System.out.println("Generando mapa de IDs de entregas...");
            GenerateSubmissionIDMap idMapGenerator = new GenerateSubmissionIDMap(
                taskDir.toString(),
                new com.github.edufeedai.model.DigestSHA1()
            );
            com.github.edufeedai.model.SubmissionIdMap[] submissionIdMaps = idMapGenerator.generateSubmissionIDMaps();
            String idMapFileName = idMapGenerator.saveSubmissionIDMaps(submissionIdMaps, "submission_id_map.json");
            if (idMapFileName != null) {
                System.out.println("✓ Mapa de IDs generado: " + idMapFileName);
            } else {
                System.err.println("⚠ No se pudo generar el mapa de IDs");
            }

            // Generar archivo JSONL con referencias a los archivos subidos
            System.out.println("\n" + "=".repeat(60));
            System.out.println("Generando archivo JSONL para Batch API...");

            // Generar JSONL (usando el mismo digest que para submission_id_map)
            BatchJSONLGenerator jsonlGenerator = new BatchJSONLGenerator(
                conn,
                taskId,
                taskDir.toString(),
                instructions,
                new com.github.edufeedai.model.DigestSHA1()
            );

            jsonlFilePath = jsonlGenerator.generateJsonl();
            System.out.println("✓ Archivo JSONL generado: " + jsonlFilePath);

        } // Fin del try-with-resources de Connection

        // === Fase 2: Subir JSONL a OpenAI ===
        System.out.println("\n" + "=".repeat(60));
        System.out.println("=== Fase 2: Subir JSONL y crear Batch Job ===\n");

        System.out.println("Subiendo archivo JSONL a OpenAI...");

        String batchFileId;
        try (OpenAIFileManagement fileManager = new OpenAIFileManagement(apiKey)) {
            batchFileId = fileManager.uploadBatchFile(jsonlFilePath);
            System.out.println("✓ JSONL subido exitosamente");
            System.out.println("  File ID: " + batchFileId);
        } catch (Exception e) {
            System.err.println("✗ Error al subir JSONL a OpenAI: " + e.getMessage());
            throw new IOException("Error al subir JSONL", e);
        }

        // === Fase 3: Crear Batch Job ===
        System.out.println("\nCreando batch job en OpenAI...");

        OpenAIBatchProcess batchProcess = new OpenAIBatchProcess(apiKey);
        BatchJob batchJob;
        try {
            batchJob = batchProcess.enqueueBatchProcess(batchFileId);
            System.out.println("✓ Batch job creado exitosamente");
            System.out.println("  Batch ID: " + batchJob.getId());
            System.out.println("  Estado: " + batchJob.getStatus());
        } catch (OpenAIAPIException e) {
            System.err.println("✗ Error al crear batch job: " + e.getMessage());
            throw new IOException("Error al crear batch job", e);
        }

        // === Actualizar base de datos con batch_id ===
        System.out.println("\nActualizando base de datos con información del batch...");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath())) {
            PreparedStatement updateBatchStmt = conn.prepareStatement(
                "UPDATE submissions SET batch_id = ?, status = 'processing', updated_at = ? WHERE task_id = ?"
            );
            updateBatchStmt.setString(1, batchJob.getId());
            updateBatchStmt.setLong(2, System.currentTimeMillis() / 1000);
            updateBatchStmt.setInt(3, taskId);
            int updatedRows = updateBatchStmt.executeUpdate();
            updateBatchStmt.close();

            System.out.println("✓ " + updatedRows + " entregas actualizadas con batch_id");
        } catch (SQLException e) {
            System.err.println("Error al acceder a la base de datos: " + e.getMessage());
            throw e;
        }

        // === Resumen final ===
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✓ Procesamiento completado exitosamente");
        System.out.println("\nResumen:");
        System.out.println("  - Archivos procesados: " + totalFilesProcessed);
        System.out.println("  - JSONL generado: " + new File(jsonlFilePath).getName());
        System.out.println("  - Batch ID: " + batchJob.getId());
        System.out.println("  - Estado actual: " + batchJob.getStatus());
        System.out.println("\nPróximos pasos:");
        System.out.println("  - Usa 'check' para consultar el estado del batch");
        System.out.println("  - Usa 'download' cuando el batch esté completado");
    }

    // Comando: check [numero_tarea]
    private static void checkCommand(Integer taskNumber) throws SQLException, IOException {
        // Verificar API key
        String apiKey = dotenv.get("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Error: La variable de entorno OPENAI_API_KEY no está configurada.");
            System.err.println("Por favor, configura tu API key de OpenAI en el archivo .env o como variable de entorno del sistema.");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath())) {
            String batchId;
            int taskId;

            if (taskNumber == null) {
                // Sin parámetro: obtener el batch más reciente de TODAS las tareas
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                    "SELECT DISTINCT s.batch_id, s.task_id " +
                    "FROM submissions s " +
                    "WHERE s.batch_id IS NOT NULL " +
                    "ORDER BY s.id DESC LIMIT 1"
                );

                if (!rs.next()) {
                    System.out.println("No hay batches procesados aún.");
                    System.out.println("Ejecuta 'process <tarea>' primero para enviar entregas a OpenAI.");
                    return;
                }

                batchId = rs.getString("batch_id");
                taskId = rs.getInt("task_id");
            } else {
                // Con parámetro: obtener el batch de la tarea específica
                // Primero, obtener el task_id real por número de lista
                Statement stmt = conn.createStatement();
                ResultSet tasksRs = stmt.executeQuery(
                    "SELECT id FROM tasks ORDER BY created_at DESC"
                );

                int currentNumber = 0;
                int realTaskId = -1;
                boolean found = false;

                while (tasksRs.next()) {
                    currentNumber++;
                    if (currentNumber == taskNumber) {
                        realTaskId = tasksRs.getInt("id");
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    System.err.println("Error: No existe la tarea número " + taskNumber);
                    System.err.println("Usa 'status' sin argumentos para ver las tareas disponibles.");
                    return;
                }

                // Ahora obtener el batch_id de esa tarea
                PreparedStatement batchStmt = conn.prepareStatement(
                    "SELECT DISTINCT batch_id FROM submissions WHERE task_id = ? AND batch_id IS NOT NULL ORDER BY id DESC LIMIT 1"
                );
                batchStmt.setInt(1, realTaskId);
                ResultSet batchRs = batchStmt.executeQuery();

                if (!batchRs.next()) {
                    System.out.println("La tarea #" + taskNumber + " no tiene batches procesados.");
                    System.out.println("Ejecuta 'process " + taskNumber + "' primero para enviar entregas a OpenAI.");
                    return;
                }

                batchId = batchRs.getString("batch_id");
                taskId = realTaskId;
                batchRs.close();
                batchStmt.close();
            }

            long interval = getBatchStatusCheckInterval();
            System.out.println("Consultando estado del batch...");
            System.out.println("(Los estados se actualizan cada " + interval + " segundos)\n");
            System.out.println("Batch ID: " + batchId);
            System.out.println("-".repeat(60));

            // Verificar si debemos actualizar desde OpenAI
            boolean shouldUpdate = shouldUpdateBatchStatus(conn, taskId);
            BatchJob job;
            String statusSource;

            if (shouldUpdate) {
                System.out.println("Consultando OpenAI API...\n");
                statusSource = "OpenAI API";

                try {
                    // Consultar estado en OpenAI
                    OpenAIBatchProcess batchProcess = new OpenAIBatchProcess(apiKey);
                    job = batchProcess.getBatchJob(batchId);

                    // Actualizar estado en la base de datos
                    String newStatus = mapBatchStatusToSubmissionStatus(job.getStatus());
                    PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE submissions SET status = ? WHERE batch_id = ?"
                    );
                    updateStmt.setString(1, newStatus);
                    updateStmt.setString(2, batchId);
                    updateStmt.executeUpdate();
                    updateStmt.close();

                    // Actualizar caché
                    updateCachedBatchStatus(conn, taskId, job.getStatus());

                } catch (OpenAIAPIException e) {
                    System.err.println("Error al consultar OpenAI API: " + e.getMessage());
                    throw new IOException("Error en API de OpenAI", e);
                }
            } else {
                // Usar estado cacheado
                System.out.println("Usando estado cacheado (última actualización hace menos de " + interval + "s)\n");
                statusSource = "caché local";

                String cachedStatus = getCachedBatchStatus(conn, taskId);
                if (cachedStatus == null) {
                    System.out.println("No hay estado cacheado disponible. Forzando actualización...\n");
                    statusSource = "OpenAI API";

                    // Consultar desde OpenAI
                    try {
                        OpenAIBatchProcess bp = new OpenAIBatchProcess(apiKey);
                        job = bp.getBatchJob(batchId);

                        // Actualizar BD y caché
                        String newStatus = mapBatchStatusToSubmissionStatus(job.getStatus());
                        PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE submissions SET status = ? WHERE batch_id = ?"
                        );
                        updateStmt.setString(1, newStatus);
                        updateStmt.setString(2, batchId);
                        updateStmt.executeUpdate();
                        updateStmt.close();

                        updateCachedBatchStatus(conn, taskId, job.getStatus());
                    } catch (OpenAIAPIException e) {
                        System.err.println("Error al consultar OpenAI API: " + e.getMessage());
                        throw new IOException("Error en API de OpenAI", e);
                    }
                } else {
                    // Crear un BatchJob simplificado con el estado cacheado
                    job = new BatchJob();
                    job.setId(batchId);
                    job.setStatus(cachedStatus);
                }
            }

            // Mostrar información del batch
            System.out.println("Estado: " + job.getStatus() + " (desde " + statusSource + ")");

            if (job.getRequestCounts() != null) {
                System.out.println("\nContadores de Solicitudes:");
                System.out.println("  Total:      " + job.getRequestCounts().getTotal());
                System.out.println("  Completadas: " + job.getRequestCounts().getCompleted());
                System.out.println("  Fallidas:   " + job.getRequestCounts().getFailed());
            }

            if (shouldUpdate) { // Solo mostrar estos detalles si consultamos OpenAI
                System.out.println("\nArchivos:");
                System.out.println("  Input File ID:  " + (job.getInputFileId() != null ? job.getInputFileId() : "-"));
                System.out.println("  Output File ID: " + (job.getOutputFileId() != null ? job.getOutputFileId() : "-"));
                System.out.println("  Error File ID:  " + (job.getErrorFileId() != null ? job.getErrorFileId() : "-"));

                if (job.getCreatedAt() != null) {
                    System.out.println("\nCreado: " + formatTimestamp(job.getCreatedAt()));
                }
                if (job.getCompletedAt() != null) {
                    System.out.println("Completado: " + formatTimestamp(job.getCompletedAt()));
                }
            }

            // Mostrar próximos pasos según el estado
            System.out.println("\n" + "=".repeat(60));
            switch (job.getStatus()) {
                case "completed":
                    System.out.println("✓ El batch ha sido completado exitosamente.");
                    System.out.println("\nPróximos pasos:");
                    System.out.println("  - Usa 'download' para descargar los resultados");
                    if (job.getOutputFileId() != null) {
                        System.out.println("  - Output File ID: " + job.getOutputFileId());
                    }
                    break;
                case "failed":
                    System.out.println("✗ El batch ha fallado.");
                    if (job.getErrorFileId() != null) {
                        System.out.println("\nError File ID: " + job.getErrorFileId());
                    }
                    break;
                case "in_progress":
                    System.out.println("⏳ El batch está en progreso...");
                    System.out.println("\nEjecuta 'check' nuevamente para ver el progreso.");
                    System.out.println("(Próxima actualización desde OpenAI en " + interval + "s)");
                    break;
                case "validating":
                case "finalizing":
                    System.out.println("⏳ " + job.getStatus() + "...");
                    break;
                default:
                    System.out.println("Estado: " + job.getStatus());
            }

        } catch (SQLException e) {
            System.err.println("Error al acceder a la base de datos: " + e.getMessage());
            throw e;
        }
    }

    // Método auxiliar: mapear estado de OpenAI a estado de submission
    private static String mapBatchStatusToSubmissionStatus(String batchStatus) {
        switch (batchStatus) {
            case "completed":
                return "completed";
            case "failed":
            case "expired":
            case "cancelled":
                return "failed";
            case "in_progress":
            case "finalizing":
                return "processing";
            default:
                return "pending";
        }
    }

    // Método auxiliar: formatear timestamp Unix
    private static String formatTimestamp(Long timestamp) {
        if (timestamp == null) return "-";
        java.time.Instant instant = java.time.Instant.ofEpochSecond(timestamp);
        java.time.ZoneId zoneId = java.time.ZoneId.systemDefault();
        java.time.format.DateTimeFormatter formatter =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return instant.atZone(zoneId).format(formatter);
    }

    // Comando: download [numero_tarea]
    private static void downloadCommand(Integer taskNumber) throws SQLException, IOException {
        // Verificar API key
        String apiKey = dotenv.get("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Error: La variable de entorno OPENAI_API_KEY no está configurada.");
            System.err.println("Por favor, configura tu API key de OpenAI en el archivo .env o como variable de entorno del sistema.");
            return;
        }

        System.out.println("Descargando resultados de OpenAI...\n");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath())) {
            String batchId;
            String taskName;

            if (taskNumber == null) {
                // Sin parámetro: obtener el batch más reciente de TODAS las tareas
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                    "SELECT DISTINCT s.batch_id, t.name " +
                    "FROM submissions s " +
                    "JOIN tasks t ON s.task_id = t.id " +
                    "WHERE s.batch_id IS NOT NULL " +
                    "ORDER BY s.id DESC LIMIT 1"
                );

                if (!rs.next()) {
                    System.out.println("No hay batches procesados aún.");
                    System.out.println("Ejecuta 'process <tarea>' primero para enviar entregas a OpenAI.");
                    return;
                }

                batchId = rs.getString("batch_id");
                taskName = rs.getString("name");
            } else {
                // Con parámetro: obtener el batch de la tarea específica
                // Primero, obtener el task_id real y el nombre por número de lista
                Statement stmt = conn.createStatement();
                ResultSet tasksRs = stmt.executeQuery(
                    "SELECT id, name FROM tasks ORDER BY created_at DESC"
                );

                int currentNumber = 0;
                int realTaskId = -1;
                String foundTaskName = null;
                boolean found = false;

                while (tasksRs.next()) {
                    currentNumber++;
                    if (currentNumber == taskNumber) {
                        realTaskId = tasksRs.getInt("id");
                        foundTaskName = tasksRs.getString("name");
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    System.err.println("Error: No existe la tarea número " + taskNumber);
                    System.err.println("Usa 'status' sin argumentos para ver las tareas disponibles.");
                    return;
                }

                taskName = foundTaskName;

                // Ahora obtener el batch_id de esa tarea
                PreparedStatement batchStmt = conn.prepareStatement(
                    "SELECT DISTINCT batch_id FROM submissions WHERE task_id = ? AND batch_id IS NOT NULL ORDER BY id DESC LIMIT 1"
                );
                batchStmt.setInt(1, realTaskId);
                ResultSet batchRs = batchStmt.executeQuery();

                if (!batchRs.next()) {
                    System.out.println("La tarea #" + taskNumber + " no tiene batches procesados.");
                    System.out.println("Ejecuta 'process " + taskNumber + "' primero para enviar entregas a OpenAI.");
                    return;
                }

                batchId = batchRs.getString("batch_id");
                batchRs.close();
                batchStmt.close();
            }
            System.out.println("Batch ID: " + batchId);

            try {
                // Consultar estado del batch
                OpenAIBatchProcess batchProcess = new OpenAIBatchProcess(apiKey);
                BatchJob job = batchProcess.getBatchJob(batchId);

                System.out.println("Estado del batch: " + job.getStatus());

                if (!"completed".equals(job.getStatus())) {
                    System.out.println("\n⚠ El batch aún no está completado.");
                    System.out.println("Estado actual: " + job.getStatus());
                    System.out.println("\nEjecuta 'check' para ver el progreso.");
                    return;
                }

                String outputFileId = job.getOutputFileId();
                if (outputFileId == null || outputFileId.isEmpty()) {
                    System.err.println("Error: No se encontró output_file_id en el batch.");
                    return;
                }

                System.out.println("Output File ID: " + outputFileId);

                // Descargar el archivo de resultados en el directorio de la tarea
                String workDir = getWorkingDirectory();
                Path taskDir = Paths.get(workDir).resolve(taskName);
                String outputFileName = "assessment_responses.jsonl";
                String outputFilePath = taskDir.resolve(outputFileName).toString();
                System.out.println("\nDescargando archivo de resultados...");
                System.out.println("Destino: " + outputFilePath);

                // Usar try-with-resources para cerrar el cliente OkHttp
                try (OpenAIFileManagement fileManager = new OpenAIFileManagement(apiKey)) {
                    fileManager.downloadFile(outputFileId, outputFilePath);
                }

                System.out.println("✓ Archivo descargado: " + outputFilePath);

                // Verificar que el archivo se descargó correctamente
                File downloadedFile = new File(outputFilePath);
                if (downloadedFile.exists() && downloadedFile.length() > 0) {
                    long fileSize = downloadedFile.length();
                    System.out.println("Tamaño del archivo: " + fileSize + " bytes");

                    // Actualizar estado en la base de datos
                    PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE submissions SET status = ? WHERE batch_id = ?"
                    );
                    updateStmt.setString(1, "downloaded");
                    updateStmt.setString(2, batchId);
                    int updated = updateStmt.executeUpdate();

                    if (updated > 0) {
                        System.out.println("\nEstado actualizado en la base de datos: downloaded");
                    }

                    System.out.println("\n" + "=".repeat(60));
                    System.out.println("✓ Descarga completada exitosamente");
                    System.out.println("\nPróximos pasos:");
                    System.out.println("  - Usa 'package' para generar el ZIP de feedback");
                } else {
                    System.err.println("\nError: El archivo descargado está vacío o no existe.");
                }

            } catch (OpenAIAPIException e) {
                System.err.println("Error al consultar/descargar desde OpenAI API: " + e.getMessage());
                throw new IOException("Error en API de OpenAI", e);
            }

        } catch (SQLException e) {
            System.err.println("Error al acceder a la base de datos: " + e.getMessage());
            throw e;
        }
    }

    // Comando: package [numero_tarea]
    private static void packageCommand(Integer taskNumber) throws SQLException, IOException {
        System.out.println("Generando paquete de feedback...\n");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath())) {
            String taskName;
            int taskId;

            if (taskNumber == null) {
                // Obtener la tarea más reciente con batch
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                    "SELECT DISTINCT t.id, t.name " +
                    "FROM submissions s " +
                    "JOIN tasks t ON s.task_id = t.id " +
                    "WHERE s.batch_id IS NOT NULL " +
                    "ORDER BY s.id DESC LIMIT 1"
                );

                if (!rs.next()) {
                    System.out.println("No hay batches procesados aún.");
                    System.out.println("Ejecuta 'process <tarea>' primero para enviar entregas a OpenAI.");
                    return;
                }

                taskId = rs.getInt("id");
                taskName = rs.getString("name");
            } else {
                // Obtener la tarea específica por número
                Statement stmt = conn.createStatement();
                ResultSet tasksRs = stmt.executeQuery(
                    "SELECT id, name FROM tasks ORDER BY created_at DESC"
                );

                int currentNumber = 0;
                String foundTaskName = null;
                int foundTaskId = -1;
                boolean found = false;

                while (tasksRs.next()) {
                    currentNumber++;
                    if (currentNumber == taskNumber) {
                        foundTaskId = tasksRs.getInt("id");
                        foundTaskName = tasksRs.getString("name");
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    System.err.println("Error: No existe la tarea número " + taskNumber);
                    System.err.println("Usa 'status' sin argumentos para ver las tareas disponibles.");
                    return;
                }

                taskId = foundTaskId;
                taskName = foundTaskName;
            }

            String workDir = getWorkingDirectory();
            Path taskDir = Paths.get(workDir).resolve(taskName);
            String idMapFile = "submission_id_map.json";
            String responsesFile = "assessment_responses.jsonl";
            String zipFileName = "feedback.zip";

            // Verificar que existen los archivos necesarios en el directorio de la tarea
            File idMapFileObj = taskDir.resolve(idMapFile).toFile();
            File responsesFileObj = taskDir.resolve(responsesFile).toFile();

            if (!idMapFileObj.exists()) {
                System.err.println("Error: No se encontró el archivo " + idMapFile);
                System.err.println("Ruta esperada: " + idMapFileObj.getAbsolutePath());
                System.err.println("Asegúrate de haber ejecutado 'process' primero.");
                return;
            }

            if (!responsesFileObj.exists()) {
                System.err.println("Error: No se encontró el archivo " + responsesFile);
                System.err.println("Ruta esperada: " + responsesFileObj.getAbsolutePath());
                System.err.println("Ejecuta 'download' primero para descargar los resultados de OpenAI.");
                return;
            }

            System.out.println("Archivos encontrados:");
            System.out.println("  - " + idMapFile + " (" + idMapFileObj.length() + " bytes)");
            System.out.println("  - " + responsesFile + " (" + responsesFileObj.length() + " bytes)");

            // Usar ZipFeedbackPackager para generar el ZIP
            System.out.println("\nGenerando ZIP de feedback...");
            ZipFeedbackPackager packager = new ZipFeedbackPackager(
                taskDir.toString(),
                idMapFile,
                responsesFile,
                zipFileName
            );
            packager.generateFeedbackZip();

            // Verificar que el ZIP se creó correctamente
            File zipFile = taskDir.resolve(zipFileName).toFile();
            if (zipFile.exists() && zipFile.length() > 0) {
                System.out.println("✓ ZIP generado: " + zipFileName);
                System.out.println("Tamaño: " + zipFile.length() + " bytes");

                // Actualizar estado en la base de datos usando el taskId que ya tenemos
                PreparedStatement batchStmt = conn.prepareStatement(
                    "SELECT DISTINCT batch_id FROM submissions WHERE task_id = ? AND batch_id IS NOT NULL ORDER BY id DESC LIMIT 1"
                );
                batchStmt.setInt(1, taskId);
                ResultSet batchRs = batchStmt.executeQuery();

                if (batchRs.next()) {
                    String batchId = batchRs.getString("batch_id");

                    PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE submissions SET status = ? WHERE batch_id = ?"
                    );
                    updateStmt.setString(1, "packaged");
                    updateStmt.setString(2, batchId);
                    int updated = updateStmt.executeUpdate();
                    updateStmt.close();

                    if (updated > 0) {
                        System.out.println("\nEstado actualizado en la base de datos: packaged");
                    }
                }
                batchRs.close();
                batchStmt.close();

                System.out.println("\n" + "=".repeat(60));
                System.out.println("✓ Paquete de feedback generado exitosamente");
                System.out.println("\nEl archivo " + zipFileName + " contiene el feedback");
                System.out.println("en formato Markdown (.md) para cada estudiante.");

            } else {
                System.err.println("\nError: El archivo ZIP no se generó correctamente.");
            }

        } catch (Exception e) {
            System.err.println("Error al generar el paquete de feedback: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error generando feedback ZIP", e);
        }
    }

    // Comando: status [numero_tarea]
    private static void statusCommand(Integer taskNumber) throws SQLException {
        if (taskNumber == null) {
            // Modo lista: mostrar todas las tareas con su estado
            listTasksWithStatus();
        } else {
            // Modo detalle: mostrar detalles de una tarea específica
            showTaskDetails(taskNumber);
        }
    }

    // Muestra detalles de una tarea específica
    private static void showTaskDetails(Integer taskNumber) throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath())) {
            // Obtener la tarea por número de lista (ordenada por created_at DESC)
            Statement stmt = conn.createStatement();
            ResultSet tasksRs = stmt.executeQuery(
                "SELECT t.id, t.name, t.moodle_task_id, t.grading_config_id, " +
                "COUNT(DISTINCT s.id) as total_submissions " +
                "FROM tasks t " +
                "LEFT JOIN submissions s ON t.id = s.task_id " +
                "GROUP BY t.id, t.name, t.moodle_task_id, t.grading_config_id " +
                "ORDER BY t.created_at DESC"
            );

            // Avanzar hasta el número de tarea especificado
            int currentNumber = 0;
            int taskId = -1;
            String taskName = null;
            String moodleTaskId = null;
            int totalSubmissions = 0;
            boolean found = false;

            while (tasksRs.next()) {
                currentNumber++;
                if (currentNumber == taskNumber) {
                    taskId = tasksRs.getInt("id");
                    taskName = tasksRs.getString("name");
                    moodleTaskId = tasksRs.getString("moodle_task_id");
                    totalSubmissions = tasksRs.getInt("total_submissions");
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.err.println("Error: No existe la tarea número " + taskNumber);
                System.err.println("Usa 'status' sin argumentos para ver todas las tareas.");
                return;
            }

            System.out.println("=".repeat(60));
            System.out.println("DETALLES DE LA TAREA #" + taskNumber);
            System.out.println("=".repeat(60));
            System.out.println("Nombre: " + taskName);
            if (moodleTaskId != null) {
                System.out.println("Moodle Task ID: " + moodleTaskId);
            }
            System.out.println("Total entregas: " + totalSubmissions);

            // Obtener estadísticas de estado
            PreparedStatement statsStmt = conn.prepareStatement(
                "SELECT status, COUNT(*) as count " +
                "FROM submissions " +
                "WHERE task_id = ? " +
                "GROUP BY status"
            );
            statsStmt.setInt(1, taskId);
            ResultSet statsRs = statsStmt.executeQuery();

            System.out.println("\nEstadísticas:");
            while (statsRs.next()) {
                String status = statsRs.getString("status");
                int count = statsRs.getInt("count");
                System.out.println("  " + status + ": " + count);
            }
            statsRs.close();
            statsStmt.close();

            // Obtener batch_id si existe
            PreparedStatement batchStmt = conn.prepareStatement(
                "SELECT DISTINCT batch_id FROM submissions WHERE task_id = ? AND batch_id IS NOT NULL"
            );
            batchStmt.setInt(1, taskId);
            ResultSet batchRs = batchStmt.executeQuery();

            if (batchRs.next()) {
                String batchId = batchRs.getString("batch_id");
                System.out.println("\nBatch ID: " + batchId);
                System.out.println("\nUsa 'check " + taskNumber + "' para ver el estado del batch");
            }
            batchRs.close();
            batchStmt.close();

            // Mostrar listado de todas las entregas
            System.out.println("\n" + "=".repeat(60));
            System.out.println("ENTREGAS");
            System.out.println("=".repeat(60));

            PreparedStatement submissionsStmt = conn.prepareStatement(
                "SELECT s.id, s.student_name, s.status, s.submission_number, " +
                "COUNT(sf.id) as file_count " +
                "FROM submissions s " +
                "LEFT JOIN submission_files sf ON s.id = sf.submission_id " +
                "WHERE s.task_id = ? " +
                "GROUP BY s.id, s.student_name, s.status, s.submission_number " +
                "ORDER BY s.student_name, s.submission_number"
            );
            submissionsStmt.setInt(1, taskId);
            ResultSet submissionsRs = submissionsStmt.executeQuery();

            System.out.println(String.format("%-4s %-30s %-15s %-8s %-10s",
                "ID", "Estudiante", "Estado", "Entrega", "Archivos"));
            System.out.println("-".repeat(75));

            int submissionCount = 0;
            while (submissionsRs.next()) {
                int id = submissionsRs.getInt("id");
                String studentName = submissionsRs.getString("student_name");
                String status = submissionsRs.getString("status");
                int submissionNumber = submissionsRs.getInt("submission_number");
                int fileCount = submissionsRs.getInt("file_count");

                // Extraer el nombre del estudiante hasta el primer "_"
                int underscoreIndex = studentName.indexOf('_');
                if (underscoreIndex != -1) {
                    studentName = studentName.substring(0, underscoreIndex);
                }

                // Truncar si sigue siendo muy largo
                if (studentName.length() > 30) {
                    studentName = studentName.substring(0, 27) + "...";
                }

                System.out.println(String.format("%-4d %-30s %-15s #%-7d %-10d",
                    id, studentName, status != null ? status : "-", submissionNumber, fileCount));
                submissionCount++;
            }
            submissionsRs.close();
            submissionsStmt.close();

            if (submissionCount == 0) {
                System.out.println("No hay entregas para esta tarea.");
            }

            System.out.println("=".repeat(60));
        }
    }

    private static void listTasksWithStatus_OLD() throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath())) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT s.id, t.name as task_name, s.student_name, s.status, s.batch_id, s.submission_number " +
                "FROM submissions s " +
                "JOIN tasks t ON s.task_id = t.id " +
                "ORDER BY t.name, s.student_name"
            );

            System.out.println("\n=== Estado de Entregas ===");
            System.out.println(String.format("%-4s %-25s %-20s %-12s %-20s", "ID", "Tarea", "Estudiante", "Estado", "Batch ID"));
            System.out.println("-".repeat(85));

            int count = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                String taskName = rs.getString("task_name");
                String studentName = rs.getString("student_name");
                String estado = rs.getString("status");
                String batchId = rs.getString("batch_id");

                // Truncar nombres largos
                if (taskName != null && taskName.length() > 23) {
                    taskName = taskName.substring(0, 20) + "...";
                }
                if (studentName != null && studentName.length() > 18) {
                    studentName = studentName.substring(0, 15) + "...";
                }

                // Truncar batch_id si es muy largo
                String batchIdDisplay = (batchId != null && !batchId.isEmpty()) ? batchId : "-";
                if (batchIdDisplay.length() > 18) {
                    batchIdDisplay = batchIdDisplay.substring(0, 15) + "...";
                }

                System.out.printf("%-4d %-25s %-20s %-12s %-20s%n",
                    id, taskName, studentName, estado != null ? estado : "-", batchIdDisplay);
                count++;
            }

            if (count == 0) {
                System.out.println("No hay entregas registradas.");
                System.out.println("\nEjecuta 'init' para inicializar el workspace y 'add <archivo.zip>' para añadir entregas.");
            } else {
                System.out.println("-".repeat(85));
                System.out.println("Total: " + count + " entregas");
            }
        } catch (SQLException e) {
            System.err.println("No se pudo leer la base de datos: " + e.getMessage());
            System.err.println("\nAsegúrate de haber ejecutado 'init' para inicializar el workspace.");
        }
    }

    // Método auxiliar: extraer el Moodle Task ID del nombre del archivo ZIP
    // Formato esperado: nombre-tarea-8608030.zip
    // Extrae el número entre el último '-' y el '.zip'
    private static String extractMoodleTaskId(String zipFileName) {
        // Quitar la extensión .zip
        String nameWithoutExtension = zipFileName.replaceFirst("[.][^.]+$", "");

        // Buscar el último guion
        int lastDashIndex = nameWithoutExtension.lastIndexOf('-');

        // Si no hay guion, no hay ID de Moodle
        if (lastDashIndex == -1 || lastDashIndex == nameWithoutExtension.length() - 1) {
            return null;
        }

        // Extraer la parte después del último guion
        String potentialId = nameWithoutExtension.substring(lastDashIndex + 1);

        // Verificar que sea un número
        if (potentialId.matches("\\d+")) {
            return potentialId;
        }

        return null;
    }

    // Método auxiliar: descomprimir un archivo ZIP
    public static void unzip(String zipFilePath, Path destDir) throws IOException {
        
        Path destAbs = destDir.toAbsolutePath().normalize();
        Files.createDirectories(destAbs);

        Path zipPath = Path.of(zipFilePath);

        try (ZipFile zf = new ZipFile(zipPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();

                Path outPath = destAbs.resolve(name).normalize();
                if (!outPath.startsWith(destAbs)) {
                    throw new IOException("Zip Slip: " + name);
                }

                if (entry.isDirectory() || name.endsWith("/")) {
                    Files.createDirectories(outPath);
                    continue;
                }

                Path parent = outPath.getParent();
                if (parent != null) Files.createDirectories(parent);

                try (InputStream is = new BufferedInputStream(zf.getInputStream(entry));
                     OutputStream os = new BufferedOutputStream(
                             Files.newOutputStream(outPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
                    is.transferTo(os);
                }
            }
        }

    }

    private static void createDatabase(Path workDir) throws SQLException {
        Path configPath = workDir.resolve(CONFIG_FOLDER);
        Path dbPath = configPath.resolve("edufeedai.db");

        try {
            // Crear el directorio .edufeedai si no existe
            Files.createDirectories(configPath);
        } catch (IOException e) {
            throw new SQLException("No se pudo crear el directorio " + configPath, e);
        }

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toString())) {
            Statement stmt = conn.createStatement();

            // Habilitar foreign keys en SQLite
            stmt.executeUpdate("PRAGMA foreign_keys = ON");

            // Tabla: grading_config (configuraciones de corrección)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS grading_config (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "message_role_system TEXT, " +
                "context TEXT, " +
                "activity_statement TEXT, " +
                "rubric TEXT, " +
                "generated_instructions TEXT, " +
                "created_at INTEGER)");

            // Tabla: tasks (tareas/actividades)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "moodle_task_id TEXT, " +
                "grading_config_id INTEGER, " +
                "created_at INTEGER NOT NULL, " +
                "updated_at INTEGER, " +
                "last_check_timestamp INTEGER, " +
                "cached_batch_status TEXT, " +
                "FOREIGN KEY (grading_config_id) REFERENCES grading_config(id))");

            // Tabla: submissions (entregas de estudiantes)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS submissions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "task_id INTEGER NOT NULL, " +
                "student_name TEXT NOT NULL, " +
                "submission_number INTEGER DEFAULT 1, " +
                "submission_id TEXT UNIQUE, " +
                "batch_id TEXT, " +
                "status TEXT DEFAULT 'pending', " +
                "feedback TEXT, " +
                "grade REAL, " +
                "submitted_at INTEGER NOT NULL, " +
                "processed_at INTEGER, " +
                "created_at INTEGER NOT NULL, " +
                "updated_at INTEGER, " +
                "FOREIGN KEY (task_id) REFERENCES tasks(id), " +
                "UNIQUE(task_id, student_name, submission_number))");

            // Tabla: submission_files (archivos de cada entrega)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS submission_files (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "submission_id INTEGER NOT NULL, " +
                "file_path TEXT NOT NULL, " +
                "file_name TEXT NOT NULL, " +
                "file_type TEXT, " +
                "file_size INTEGER, " +
                "openai_file_id TEXT, " +
                "is_text_file INTEGER DEFAULT 0, " +
                "content_extracted TEXT, " +
                "created_at INTEGER NOT NULL, " +
                "FOREIGN KEY (submission_id) REFERENCES submissions(id) ON DELETE CASCADE)");

            // Índices para mejorar rendimiento
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_submissions_task_id ON submissions(task_id)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_submissions_student ON submissions(task_id, student_name)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_submission_files_submission_id ON submission_files(submission_id)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_submission_files_openai_id ON submission_files(openai_file_id)");

            // Tabla legacy para compatibilidad temporal (será eliminada en futuras versiones)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS entregas (" +
                "id INTEGER PRIMARY KEY, " +
                "nombre TEXT, " +
                "estado TEXT, " +
                "submission_id TEXT, " +
                "file_id TEXT, " +
                "batch_id TEXT)");

            // Migración: Añadir campos de caché si no existen
            try {
                // Verificar si la columna last_check_timestamp existe en tasks
                ResultSet columns = conn.getMetaData().getColumns(null, null, "tasks", "last_check_timestamp");
                if (!columns.next()) {
                    stmt.executeUpdate("ALTER TABLE tasks ADD COLUMN last_check_timestamp INTEGER");
                }
                columns.close();

                columns = conn.getMetaData().getColumns(null, null, "tasks", "cached_batch_status");
                if (!columns.next()) {
                    stmt.executeUpdate("ALTER TABLE tasks ADD COLUMN cached_batch_status TEXT");
                }
                columns.close();

                // Verificar si la columna updated_at existe en submissions
                columns = conn.getMetaData().getColumns(null, null, "submissions", "updated_at");
                if (!columns.next()) {
                    stmt.executeUpdate("ALTER TABLE submissions ADD COLUMN updated_at INTEGER");
                }
                columns.close();
            } catch (SQLException e) {
                // Si falla la migración, puede ser que las columnas ya existan
                // No es crítico, así que solo registramos el error
                System.err.println("Advertencia: No se pudieron añadir columnas en la migración: " + e.getMessage());
            }
        }
    }

    // ========== Métodos de gestión de caché de estado de batches ==========

    /**
     * Obtiene el intervalo de comprobación de estado desde .env (en segundos).
     * Por defecto es 60 segundos.
     */
    private static long getBatchStatusCheckInterval() {
        String interval = dotenv.get("BATCH_STATUS_CHECK_INTERVAL");
        if (interval == null || interval.isEmpty()) {
            return 60; // Default: 60 segundos
        }
        try {
            return Long.parseLong(interval);
        } catch (NumberFormatException e) {
            System.err.println("Advertencia: BATCH_STATUS_CHECK_INTERVAL inválido, usando 60 segundos");
            return 60;
        }
    }

    /**
     * Verifica si debe actualizar el estado del batch desde OpenAI.
     * Retorna true si:
     * - No hay timestamp de última comprobación (null)
     * - Han pasado más de BATCH_STATUS_CHECK_INTERVAL segundos desde la última comprobación
     */
    private static boolean shouldUpdateBatchStatus(Connection conn, int taskId) throws SQLException {
        long interval = getBatchStatusCheckInterval();
        long now = System.currentTimeMillis() / 1000;

        PreparedStatement stmt = conn.prepareStatement(
            "SELECT last_check_timestamp FROM tasks WHERE id = ?"
        );
        stmt.setInt(1, taskId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            // Obtener el valor como long, que será 0 si es NULL
            long lastCheck = rs.getLong("last_check_timestamp");
            boolean wasNull = rs.wasNull();
            rs.close();
            stmt.close();

            if (wasNull || lastCheck == 0) {
                return true; // Nunca se ha comprobado
            }

            long elapsed = now - lastCheck;
            return elapsed >= interval;
        }

        rs.close();
        stmt.close();
        return true; // Si no se encuentra la tarea, actualizar
    }

    /**
     * Actualiza el timestamp de última comprobación y el estado cacheado en la BD.
     */
    private static void updateCachedBatchStatus(Connection conn, int taskId, String status) throws SQLException {
        long now = System.currentTimeMillis() / 1000;

        PreparedStatement stmt = conn.prepareStatement(
            "UPDATE tasks SET last_check_timestamp = ?, cached_batch_status = ? WHERE id = ?"
        );
        stmt.setLong(1, now);
        stmt.setString(2, status);
        stmt.setInt(3, taskId);
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Obtiene el estado cacheado del batch desde la BD.
     * Retorna null si no hay estado cacheado.
     */
    private static String getCachedBatchStatus(Connection conn, int taskId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT cached_batch_status FROM tasks WHERE id = ?"
        );
        stmt.setInt(1, taskId);
        ResultSet rs = stmt.executeQuery();

        String status = null;
        if (rs.next()) {
            status = rs.getString("cached_batch_status");
        }

        rs.close();
        stmt.close();
        return status;
    }

    // Punto de extensión: agregar métodos auxiliares para lógica de negocio

    /**
     * Carga el contenido del archivo openaisystem.config desde los recursos
     */
    private static String loadSystemConfigFromResources() {
        try (InputStream inputStream = App.class.getClassLoader().getResourceAsStream("openaisystem.config")) {
            if (inputStream == null) {
                throw new IllegalStateException("No se pudo encontrar el archivo openaisystem.config en los recursos");
            }
            return new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Error al leer el archivo openaisystem.config desde los recursos", e);
        }
    }
}

# edufeedai-cli

Este módulo es la **interfaz de línea de comandos** y el **punto de entrada principal** del proyecto EduFeedAI.

Herramienta para generar feedback automatizado de entregas de estudiantes utilizando la API de OpenAI.

## ⚙️ Configuración

### Archivo .env

El CLI utiliza un archivo `.env` para la configuración. Crea un archivo `.env` en la raíz del proyecto (puedes copiar `.env.example` como punto de partida):

```bash
cp .env.example .env
```

### Variables de Entorno

#### OPENAI_API_KEY (Requerida)
Tu clave de API de OpenAI. Es necesaria para los comandos `process`, `check` y `download`.

```env
OPENAI_API_KEY=sk-your-api-key-here
```

#### WORK_DIR (Opcional)
Directorio de trabajo donde se procesarán los archivos. Si no se especifica, se usa el directorio actual (`.`).

```env
WORK_DIR=/ruta/a/tu/directorio/de/trabajo
```

Cuando se especifica `WORK_DIR`:
- Los archivos ZIP de entregas se descomprimirán aquí
- La base de datos SQLite se creará en `WORK_DIR/.edufeedai/edufeedai.db`
- Todos los archivos generados (JSONL, mapas de ID, feedback ZIP) se guardarán aquí

## 🚀 Compilación y Ejecución

Desde la raíz del proyecto, compila todo:
```bash
mvn clean install
```

## 📋 Comandos Disponibles

### 1. init - Inicializar

Descomprime el ZIP de entregas y crea la base de datos:

```bash
cd edufeedai-cli
mvn exec:java -Dexec.args="init /ruta/a/entregas.zip"
```

Si `WORK_DIR` está configurado en `.env`, el ZIP se descomprimirá en ese directorio.

### 2. process - Procesar entregas

Procesa las entregas y las envía a OpenAI:

```bash
mvn exec:java -Dexec.args="process"
```

### 3. check - Verificar estado

Consulta el estado del batch en OpenAI:

```bash
mvn exec:java -Dexec.args="check"
```

### 4. download - Descargar resultados

Descarga los resultados cuando el batch esté completado:

```bash
mvn exec:java -Dexec.args="download"
```

### 5. package - Generar paquete de feedback

Genera el ZIP con los archivos Markdown de feedback:

```bash
mvn exec:java -Dexec.args="package"
```

### 6. status - Ver estado de entregas

Muestra el estado actual de todas las entregas:

```bash
mvn exec:java -Dexec.args="status"
```

### 7. help - Ayuda

Muestra la ayuda con todos los comandos disponibles:

```bash
mvn exec:java -Dexec.args="help"
```

## 💡 Ejemplo de flujo completo

```bash
# 1. Configurar variables de entorno
cat > .env << EOF
OPENAI_API_KEY=sk-your-api-key-here
WORK_DIR=/home/usuario/entregas-proyecto
EOF

# 2. Compilar
mvn clean install

# 3. Ejecutar flujo completo
cd edufeedai-cli
mvn exec:java -Dexec.args="init /ruta/a/entregas.zip"
mvn exec:java -Dexec.args="process"
mvn exec:java -Dexec.args="check"
# Esperar a que el batch esté completado...
mvn exec:java -Dexec.args="download"
mvn exec:java -Dexec.args="package"
mvn exec:java -Dexec.args="status"
```

## 📦 Archivos generados

Todos los archivos se generan en el directorio de trabajo (`WORK_DIR` o directorio actual):

- `.edufeedai/edufeedai.db` - Base de datos SQLite
- `submission_id_map.json` - Mapeo de IDs de entregas
- `*vol1de1.jsonl` - Archivo JSONL enviado a OpenAI
- `assessment_responses.jsonl` - Respuestas descargadas de OpenAI
- `feedback.zip` - Archivo final con feedback en Markdown

## 📊 Estados de las entregas

- `pending` - Registrada pero no procesada
- `processing` - Enviada a OpenAI, en proceso
- `completed` - Procesamiento completado en OpenAI
- `downloaded` - Resultados descargados
- `packaged` - ZIP de feedback generado
- `failed` - El batch falló en OpenAI

## 🏗️ JAR ejecutable (opcional)

Si configuras el plugin Shade en el `pom.xml`, puedes generar un JAR ejecutable:
```bash
mvn clean package
java -jar target/edufeedai-cli-1.0-SNAPSHOT-shaded.jar init /ruta/a/entregas.zip
```

## 📄 Notas
- Este módulo depende de `edufeedai-lib` para toda la lógica de negocio
- El archivo `.env` no se sube al repositorio (está en `.gitignore`)
- Usa `.env.example` como plantilla para configurar tu entorno

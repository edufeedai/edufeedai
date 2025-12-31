# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

EduFeedAI is a Java application for processing student assessment PDFs using OCR (Tess4J, OpenCV) and text analysis to generate automated feedback via OpenAI's Batch API. The project is organized as a multi-module Maven project with two main modules:

- **edufeedai-lib**: Core business logic, utilities, models, and reusable resources
- **edufeedai-cli**: Command-line interface and main entry point

## Build and Execution Commands

### Build the entire project
```bash
mvn clean install
```

### Run the CLI application
```bash
cd edufeedai-cli
mvn exec:java
```

Or with arguments:
```bash
mvn exec:java -Dexec.args="init path/to/file.zip"
mvn exec:java -Dexec.args="process"
mvn exec:java -Dexec.args="status"
```

### Run tests

All tests (from project root):
```bash
mvn test
```

Run only unit tests (excludes integration tests):
```bash
mvn test -Dgroups="!integration"
```

Run only integration tests:
```bash
mvn test -Dgroups="integration"
```

Run tests in a specific module:
```bash
cd edufeedai-lib
mvn test
```

Run a single test class:
```bash
mvn test -Dtest=TextSerializerTest
```

### Build executable JAR (if Shade plugin is configured)
```bash
cd edufeedai-cli
mvn clean package
java -jar target/edufeedai-cli-1.0-SNAPSHOT-shaded.jar
```

## Architecture Overview

### Core Workflow

The application follows a multi-stage pipeline for processing student assessments:

1. **File Extraction**: Student submissions (typically PDFs in ZIP archives) are extracted
2. **Content Processing**: PDFs are processed using OCR to extract text and images
3. **Serialization**: File contents are concatenated and serialized into JSON format
4. **Batch Submission**: JSON requests are sent to OpenAI's Batch API for processing
5. **Feedback Generation**: Results are downloaded and packaged for distribution

### Key Components

**edufeedai-lib** contains the core processing pipeline:

- **FileConcatenator**: Recursively scans directories, filters files, concatenates content with markers (`>>>{filename}` and `<<<{filename}`), and generates JSON Lines for OpenAI API. Uses digest-based naming (SHA1/MD5/SHA256/SHA512) for output files.

- **TextSerializer**: Manages serialization of student submissions into JSONL volumes for batch processing. Handles JSON file discovery, concatenation, and volume splitting.

- **OpenAI Integration** (`model.openai.platform.api`):
  - `OpenAIBatchProcess`: Manages batch job submission and retrieval
  - `OpenAICorrectionPromptBuilder`: Builds prompts for assessment correction
  - `OpenAIFileManagement`: Handles file upload/download to OpenAI

- **PDF Processing** (`model`):
  - `PDFExtractTextAndImagesOrdered`: Extracts text and images from PDFs in order
  - `Assessment`, `AssessmentBase`, `CheckResults`: Models for grading configuration and results
  - `ContentBlock`: Represents text/image blocks from PDF extraction

- **OCR Processing** (`model.ocrlib`):
  - `OCRProcessorTesseract`: Tesseract OCR implementation
  - `OCROpenCVImagePreprocess`: OpenCV-based image preprocessing

- **Utilities**:
  - `ZipUtils`, `ZipFeedbackPackager`: ZIP archive handling for feedback distribution
  - `GenerateSubmissionIDMap`: Maps student submissions to unique identifiers
  - Digest implementations (SHA1/256/512, MD5) for file identification

**edufeedai-cli** provides the command interface:

- **App**: Main entry point with commands:
  - `init <archivo.zip>`: Unzips submissions and creates SQLite database
  - `process`: Processes submissions (generates TXT/JSONL using TextSerializer)
  - `status`: Shows processing status from database
  - `help`: Displays usage information

- **DatabaseInitializer**: Sets up SQLite database in `.edufeedai/` directory with `entregas` table tracking submission state

### Testing Structure

Tests use custom JUnit 5 annotations for categorization:

- `@IntegrationTest`: Integration tests (tagged "integration")
- `@PrivacyTest`: Privacy-related tests
- `@FileIOTest`: File I/O tests
- `@UtilityTest`: Utility function tests

Integration tests may require external resources (OpenAI API keys, test PDFs) and are typically excluded from default test runs.

## Technology Stack

- **Java 17** (source/target compatibility)
- **Maven** for build management
- **Apache PDFBox 3.0.3** for PDF processing
- **Tess4J 5.13.0** for OCR
- **OpenCV 4.6.0** for image preprocessing
- **Apache HttpClient 5.5** for API communication
- **Gson 2.10.1** for JSON serialization
- **SQLite JDBC 3.45.3.0** (CLI module only)
- **JUnit 5** and **Mockito** for testing
- **SLF4J + Logback** for logging

## Environment Configuration

- **OpenAI API Key**: Required for integration tests and production use. Set via environment variable or `.env` file (gitignored).
- **Tesseract**: OCR tests require Tesseract installed with language data. Maven Surefire is configured with `-Djava.library.path=/usr/lib/jni` for native library access.
- **SQLite Database**: CLI creates `.edufeedai/edufeedai.db` in the working directory for tracking submission state.

## Important File Filtering

`FileConcatenator` excludes common non-code files:
- Hidden files and directories
- Media files (images, audio, video)
- Archives (zip, rar, tar, gz, etc.)
- Office documents (doc, xls, ppt, pdf)
- Node.js artifacts (node_modules, package.json, package-lock.json)
- Build/config files (Dockerfile, LICENSE, tsconfig.json)

When modifying file filtering logic, update the `accept()` method in `FileConcatenator.java`.

## Development Notes

- The CLI is in early development (commands have TODO markers for business logic extension points)
- All modules share the parent POM version `1.0-SNAPSHOT`
- GroupId: `com.github.amiguetes.edufeedai`
- The project uses SLF4J for logging throughout; prefer logger instances over System.out/err
- JSON Line format is used for OpenAI Batch API communication (one JSON object per line)

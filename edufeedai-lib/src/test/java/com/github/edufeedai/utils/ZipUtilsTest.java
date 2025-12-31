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

package com.github.edufeedai.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ZipUtilsTest {

    @TempDir
    Path tempDir;

    Path testDirectoryPath;

    private final String testDirPath = "testDir";
    private final String zipDirPath = "zipDir";

    private final String testDirFile = testDirPath + ".zip";
    private final String zipDirFile = zipDirPath + ".zip";

    private final String testFile1 = "file1.txt";
    private final String testFile2 = "file2.txt";
    private final String testFile3 = "file3.txt";
    private final String testFile4 = "file4.txt";

    @BeforeEach
    public void setUp() throws IOException {

        String testDirPathString = "test-"  + getClass().getName() + "-" + System.currentTimeMillis();
        testDirectoryPath = tempDir.resolve(testDirPathString);
        
        Files.createDirectory(testDirectoryPath);

        Path testPath = testDirectoryPath.resolve(testDirPath);
        Files.createDirectories(testPath);

        Files.createFile(testPath.resolve(testFile1));
        Files.createFile(testPath.resolve(testFile2));

        Path zipPath = testDirectoryPath.resolve(zipDirPath);
        Files.createDirectories(zipPath);

        Files.createFile(zipPath.resolve(testFile3));
        Files.createFile(zipPath.resolve(testFile4));

        try (FileOutputStream fos = new FileOutputStream(testDirectoryPath.resolve(testDirFile).toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            zipDirectory(testPath, testPath.getFileName().toString(), zos);
        }
    }

    @AfterEach
    public void tearDown() throws IOException {
        deleteDirectory(testDirectoryPath);
    }

    @Test
    public void testUnzipAndRemove() throws IOException {

        ZipUtils.unzipAndRemove(testDirectoryPath);

        assertFalse(Files.exists(testDirectoryPath.resolve(testDirFile)));

        String relFile1 = testDirPath + File.separator + testFile1;
        String relFile2 = testDirPath + File.separator + testFile2;

        assertTrue(Files.exists(testDirectoryPath.resolve(relFile1)));
        assertTrue(Files.exists(testDirectoryPath.resolve(relFile2)));
    }

    @Test
    public void testCompressAndRemoveDirectories() throws IOException {

        ZipUtils.compressFileAndRemoveDirectories(testDirectoryPath.resolve(testDirPath), ".txt");

        assertFalse(Files.exists(testDirectoryPath.resolve(testDirPath)));
        assertTrue(Files.exists(testDirectoryPath.resolve(testDirFile)));

    }

    private void zipDirectory(Path folder, String parentFolder, ZipOutputStream zos) throws IOException {

        Files.walk(folder).forEach(path -> {
            if (Files.isDirectory(path)) {
                return;
            }
            String zipEntryName = parentFolder + "/" + folder.relativize(path).toString().replace("\\", "/");
            try {
                zos.putNextEntry(new ZipEntry(zipEntryName));
                Files.copy(path, zos);
                zos.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void deleteDirectory(Path directory) throws IOException {
        Files.walk(directory)
                .sorted(Comparator.reverseOrder())
             .forEach(path -> {
                 try {
                     Files.delete(path);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             });
    }
}

/*
 *    Copyright 2009-2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExternalResourcesTest {

  private File sourceFile;
  private File destFile;
  private File badFile;
  private File tempFile;

  @BeforeEach
  void setUp() throws Exception {
    tempFile = Files.createTempFile("migration", "properties").toFile();
    tempFile.canWrite();
    sourceFile = Files.createTempFile("test1", "sql").toFile();
    destFile = Files.createTempFile("test2", "sql").toFile();
  }

  @Test
  void testcopyExternalResource() {
    assertDoesNotThrow(() -> {
      ExternalResources.copyExternalResource(sourceFile, destFile);
    });
  }

  @Test
  void testcopyExternalResource_fileNotFound() {

    try {
      badFile = Path.of("/tmp/nofile.sql").toFile();
      ExternalResources.copyExternalResource(badFile, destFile);
    } catch (Exception e) {
      assertTrue(e instanceof NoSuchFileException);
    }
  }

  @Test
  void testcopyExternalResource_emptyStringAsFile() {

    try {
      badFile = Path.of(" ").toFile();
      ExternalResources.copyExternalResource(badFile, destFile);
    } catch (Exception e) {
      assertTrue(e instanceof InvalidPathException || e instanceof NoSuchFileException);
    }
  }

  @Test
  void getConfiguredTemplate() {
    String templateName = "";

    try (BufferedWriter fileWriter = Files.newBufferedWriter(tempFile.toPath(), StandardCharsets.UTF_8)) {
      fileWriter.append("new_command.template=templates/col_new_template_migration.sql");
      fileWriter.flush();
      templateName = ExternalResources.getConfiguredTemplate(tempFile.getAbsolutePath(), "new_command.template");
      assertEquals("templates/col_new_template_migration.sql", templateName);
    } catch (Exception e) {
      fail("Test failed with exception: " + e.getMessage());
    }
  }

  // New test cases for enhanced coverage

  @Test
  void shouldCopyFileContentCorrectly() throws IOException {
    File testSource = Files.createTempFile("source", ".txt").toFile();
    File testDest = Files.createTempFile("dest", ".txt").toFile();
    String content = "Hello, World!";

    Files.writeString(testSource.toPath(), content);

    ExternalResources.copyExternalResource(testSource, testDest);

    String copiedContent = Files.readString(testDest.toPath());
    assertEquals(content, copiedContent);
  }

  @Test
  void shouldOverwriteExistingDestination() throws IOException {
    File testSource = Files.createTempFile("source", ".txt").toFile();
    File testDest = Files.createTempFile("dest", ".txt").toFile();
    String newContent = "New content";
    String oldContent = "Old content";

    Files.writeString(testSource.toPath(), newContent);
    Files.writeString(testDest.toPath(), oldContent);

    ExternalResources.copyExternalResource(testSource, testDest);

    String copiedContent = Files.readString(testDest.toPath());
    assertEquals(newContent, copiedContent);
  }

  @Test
  void shouldCopyEmptyFile() throws IOException {
    File testSource = Files.createTempFile("empty", ".txt").toFile();
    File testDest = Files.createTempFile("dest", ".txt").toFile();

    ExternalResources.copyExternalResource(testSource, testDest);

    byte[] sourceBytes = Files.readAllBytes(testSource.toPath());
    byte[] destBytes = Files.readAllBytes(testDest.toPath());
    assertArrayEquals(sourceBytes, destBytes);
  }

  @Test
  void shouldCopyFileWithBinaryContent() throws IOException {
    File testSource = Files.createTempFile("binary", ".bin").toFile();
    File testDest = Files.createTempFile("dest", ".bin").toFile();
    byte[] binaryData = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, (byte) 255, (byte) 128, 64, 32, 16 };

    Files.write(testSource.toPath(), binaryData);

    ExternalResources.copyExternalResource(testSource, testDest);

    byte[] copiedData = Files.readAllBytes(testDest.toPath());
    assertArrayEquals(binaryData, copiedData);
  }

  @Test
  void shouldThrowExceptionWhenSourceIsNull() throws IOException {
    assertThrows(NullPointerException.class, () -> ExternalResources.copyExternalResource(null, destFile));
  }

  @Test
  void shouldCopyFileWithSizeExactly4096Bytes() throws IOException {
    File testSource = Files.createTempFile("exact4096", ".txt").toFile();
    File testDest = Files.createTempFile("dest", ".txt").toFile();
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < 4096; i++) {
      builder.append("x");
    }
    String content = builder.toString();

    Files.writeString(testSource.toPath(), content);

    ExternalResources.copyExternalResource(testSource, testDest);

    assertEquals(content, Files.readString(testDest.toPath()));
    assertEquals(4096, testDest.length());
  }

  @Test
  void shouldThrowExceptionWhenDestPathContainsNonExistentDirectory() throws IOException {
    File testSource = Files.createTempFile("source", ".txt").toFile();
    Path nonExistentDir = Path.of(System.getProperty("java.io.tmpdir"), "nonexistent_" + System.currentTimeMillis(),
        "dest.txt");
    File testDest = nonExistentDir.toFile();

    assertThrows(Exception.class, () -> ExternalResources.copyExternalResource(testSource, testDest));
  }

  @AfterEach
  void cleanUp() {
    sourceFile.delete();
    destFile.delete();
    tempFile.delete();
  }
}

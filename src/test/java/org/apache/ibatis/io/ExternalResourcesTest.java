/*
 *    Copyright 2009-2025 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedWriter;
import java.io.File;
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

  /**
   * @throws java.lang.Exception
   */
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

  @AfterEach
  void cleanUp() {
    sourceFile.delete();
    destFile.delete();
    tempFile.delete();
  }
}

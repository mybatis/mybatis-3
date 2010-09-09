package org.apache.ibatis.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExternalResourcesTest {

  private File sourceFile;
  private File destFile;
  private File badFile;
  private File tempFile;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    tempFile = File.createTempFile("migration", "properties");
    tempFile.canWrite();
    sourceFile = File.createTempFile("test1", "sql");
    destFile = File.createTempFile("test2", "sql");
  }

  @Test
  public void testcopyExternalResource() {

    try {
      ExternalResources.copyExternalResource(sourceFile, destFile);
    } catch (IOException e) {
    }

  }

  @Test
  public void testcopyExternalResource_fileNotFound() {

    try {
      badFile = new File("/tmp/nofile.sql");
      ExternalResources.copyExternalResource(badFile, destFile);
    } catch (IOException e) {
      Assert.assertEquals("/tmp/nofile.sql (No such file or directory)", e.getMessage());
    }

  }

  @Test
  public void testcopyExternalResource_emptyStringAsFile() {

    try {
      badFile = new File(" ");
      ExternalResources.copyExternalResource(badFile, destFile);
    } catch (Exception e) {
      Assert.assertEquals("(No such file or directory)", e.getMessage().trim());
    }

  }

  @Test
  public void testGetConfiguredTemplate() {
    String templateName = "";

    try {
      FileWriter fileWriter = new FileWriter(tempFile);
      fileWriter.append("new_command.template=templates/col_new_template_migration.sql");
      fileWriter.flush();
      templateName = ExternalResources.getConfiguredTemplate(tempFile.getAbsolutePath(), "new_command.template");
      Assert.assertEquals("templates/col_new_template_migration.sql", templateName);
    } catch (Exception e) {
      Assert.fail("Test failed with execption: " + e.getMessage());
    }
  }

  @After
  public void cleanUp() {
    sourceFile.delete();
    destFile.delete();
    tempFile.delete();
  }
}

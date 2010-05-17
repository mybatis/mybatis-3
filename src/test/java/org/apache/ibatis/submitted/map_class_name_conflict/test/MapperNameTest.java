package org.apache.ibatis.submitted.map_class_name_conflict.test;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

public class MapperNameTest {

  @Test
  public void initDatabase() throws IOException {
    String resource = "org/apache/ibatis/submitted/map_class_name_conflict/mapper/ibatisConfig.xml";
    Reader reader = Resources.getResourceAsReader(resource);
    new SqlSessionFactoryBuilder().build(reader);
  }
}

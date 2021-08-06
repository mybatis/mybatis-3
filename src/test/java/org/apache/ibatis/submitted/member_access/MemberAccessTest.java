/*
 *    Copyright 2009-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.member_access;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for member access of Java Object.
 */
class MemberAccessTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    try (
        Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/member_access/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      sqlSessionFactory.getConfiguration().addMapper(Mapper.class);
    }
  }

  @Test
  void parameterMappingAndResultAutoMapping() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      Params params = new Params();
      Bean bean = mapper.resultAutoMapping(params);

      assertEquals(params.privateField, bean.privateField);
      assertEquals(params.packagePrivateField, bean.packagePrivateField);
      assertEquals(params.protectedField, bean.protectedField);
      assertEquals(params.publicField, bean.publicField);
      assertEquals(params.getPrivateProperty(), bean.properties.get("privateProperty"));
      assertEquals(params.getPackagePrivateProperty(), bean.properties.get("packagePrivateProperty"));
      assertEquals(params.getProtectedProperty(), bean.properties.get("protectedProperty"));
      assertEquals(params.getPublicProperty(), bean.properties.get("publicProperty"));
    }
  }

  @Test // gh-1258
  void parameterMappingAndResultAutoMappingUsingOgnl() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      Params params = new Params();
      Bean bean = mapper.resultAutoMappingUsingOgnl(params);

      assertEquals(params.privateField + "%", bean.privateField);
      assertEquals(params.packagePrivateField + "%", bean.packagePrivateField);
      assertEquals(params.protectedField + "%", bean.protectedField);
      assertEquals(params.publicField + "%", bean.publicField);
      assertEquals(params.getPrivateProperty() + "%", bean.properties.get("privateProperty"));
      assertEquals(params.getPackagePrivateProperty() + "%", bean.properties.get("packagePrivateProperty"));
      assertEquals(params.getProtectedProperty() + "%", bean.properties.get("protectedProperty"));
      assertEquals(params.getPublicProperty() + "%", bean.properties.get("publicProperty"));
    }
  }

  @Test
  void parameterMappingAndResultMapping() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      Params params = new Params();
      Bean bean = mapper.resultMapping(params);

      assertEquals(params.privateField, bean.privateField);
      assertEquals(params.packagePrivateField, bean.packagePrivateField);
      assertEquals(params.protectedField, bean.protectedField);
      assertEquals(params.publicField, bean.publicField);
      assertEquals(params.getPrivateProperty(), bean.properties.get("privateProperty"));
      assertEquals(params.getPackagePrivateProperty(), bean.properties.get("packagePrivateProperty"));
      assertEquals(params.getProtectedProperty(), bean.properties.get("protectedProperty"));
      assertEquals(params.getPublicProperty(), bean.properties.get("publicProperty"));
    }
  }

  @Test
  void constructorAutoMapping() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      {
        Immutable immutable = mapper.privateConstructorAutoMapping();
        assertEquals(1, immutable.properties.size());
        assertEquals("1", immutable.properties.get("arg1"));
      }

      {
        Immutable immutable = mapper.packagePrivateConstructorAutoMapping();
        assertEquals(2, immutable.properties.size());
        assertEquals("1", immutable.properties.get("arg1"));
        assertEquals("2", immutable.properties.get("arg2"));
      }

      {
        Immutable immutable = mapper.protectedConstructorAutoMapping();
        assertEquals(3, immutable.properties.size());
        assertEquals("1", immutable.properties.get("arg1"));
        assertEquals("2", immutable.properties.get("arg2"));
        assertEquals("3", immutable.properties.get("arg3"));
      }

      {
        Immutable immutable = mapper.publicConstructorAutoMapping();
        assertEquals(4, immutable.properties.size());
        assertEquals("1", immutable.properties.get("arg1"));
        assertEquals("2", immutable.properties.get("arg2"));
        assertEquals("3", immutable.properties.get("arg3"));
        assertEquals("4", immutable.properties.get("arg4"));
      }
    }

  }

  @Test
  void constructorMapping() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      {
        Immutable immutable = mapper.privateConstructorMapping();
        assertEquals(1, immutable.properties.size());
        assertEquals("1", immutable.properties.get("arg1"));
      }

      {
        Immutable immutable = mapper.packagePrivateConstructorMapping();
        assertEquals(2, immutable.properties.size());
        assertEquals("1", immutable.properties.get("arg1"));
        assertEquals("2", immutable.properties.get("arg2"));
      }

      {
        Immutable immutable = mapper.protectedConstructorMapping();
        assertEquals(3, immutable.properties.size());
        assertEquals("1", immutable.properties.get("arg1"));
        assertEquals("2", immutable.properties.get("arg2"));
        assertEquals("3", immutable.properties.get("arg3"));
      }

      {
        Immutable immutable = mapper.publicConstructorMapping();
        assertEquals(4, immutable.properties.size());
        assertEquals("1", immutable.properties.get("arg1"));
        assertEquals("2", immutable.properties.get("arg2"));
        assertEquals("3", immutable.properties.get("arg3"));
        assertEquals("4", immutable.properties.get("arg4"));
      }
    }

  }

  interface Mapper {
    @Select({
        // @formatter:off
        "SELECT"
          ,"#{privateField} as privateField"
          ,",#{packagePrivateField} as packagePrivateField"
          ,",#{protectedField} as protectedField"
          ,",#{publicField} as publicField"
          ,",#{privateProperty} as privateProperty"
          ,",#{packagePrivateProperty} as packagePrivateProperty"
          ,",#{protectedProperty} as protectedProperty"
          ,",#{publicProperty} as publicProperty"
        ,"FROM"
          ,"INFORMATION_SCHEMA.SYSTEM_USERS"
        // @formatter:on
    })
    Bean resultAutoMapping(Params params);

    @Select({
        // @formatter:off
        "<script>"

          ,"<bind name=\"privateFieldValue\" value=\"_parameter.privateField + '%'\" />"
          ,"<bind name=\"packagePrivateFieldValue\" value=\"_parameter.packagePrivateField + '%'\" />"
          ,"<bind name=\"protectedFieldValue\" value=\"_parameter.protectedField + '%'\" />"
          ,"<bind name=\"publicFieldValue\" value=\"_parameter.publicField + '%'\" />"
          ,"<bind name=\"privatePropertyValue\" value=\"_parameter.privateProperty + '%'\" />"
          ,"<bind name=\"packagePrivatePropertyValue\" value=\"_parameter.packagePrivateProperty + '%'\" />"
          ,"<bind name=\"protectedPropertyValue\" value=\"_parameter.getProtectedProperty() + '%'\" />"
          ,"<bind name=\"publicPropertyValue\" value=\"_parameter.publicProperty + '%'\" />"

          ,"SELECT"
          ,"#{privateFieldValue} as privateField"
          ,",#{packagePrivateFieldValue} as packagePrivateField"
          ,",#{protectedFieldValue} as protectedField"
          ,",#{publicFieldValue} as publicField"
          ,",#{privatePropertyValue} as privateProperty"
          ,",#{packagePrivatePropertyValue} as packagePrivateProperty"
          ,",#{protectedPropertyValue} as protectedProperty"
          ,",#{publicPropertyValue} as publicProperty"

          ,"FROM"
            ,"INFORMATION_SCHEMA.SYSTEM_USERS"

        ,"</script>"}
        // @formatter:on
    )
    Bean resultAutoMappingUsingOgnl(Params params);

    @Results({
        // @formatter:off
        @Result(property = "privateField", column = "private_field")
        ,@Result(property = "packagePrivateField", column = "package_private_field")
        ,@Result(property = "protectedField", column = "protected_field")
        ,@Result(property = "publicField", column = "public_field")
        ,@Result(property = "privateProperty", column = "private_property")
        ,@Result(property = "packagePrivateProperty", column = "package_private_property")
        ,@Result(property = "protectedProperty", column = "protected_property")
        ,@Result(property = "publicProperty", column = "public_property")
        // @formatter:on
    })
    @Select({
        // @formatter:off
        "SELECT"
          ,"#{privateField} as private_field"
          ,",#{packagePrivateField} as package_private_field"
          ,",#{protectedField} as protected_field"
          ,",#{publicField} as public_field"
          ,",#{privateProperty} as private_property"
          ,",#{packagePrivateProperty} as package_private_property"
          ,",#{protectedProperty} as protected_property"
          ,",#{publicProperty} as public_property"
        ,"FROM"
          ,"INFORMATION_SCHEMA.SYSTEM_USERS"
        // @formatter:on
    })
    Bean resultMapping(Params params);

    @Select("SELECT '1' FROM INFORMATION_SCHEMA.SYSTEM_USERS")
    Immutable privateConstructorAutoMapping();

    @Select("SELECT '1', '2' FROM INFORMATION_SCHEMA.SYSTEM_USERS")
    Immutable packagePrivateConstructorAutoMapping();

    @Select("SELECT '1', '2', '3' FROM INFORMATION_SCHEMA.SYSTEM_USERS")
    Immutable protectedConstructorAutoMapping();

    @Select("SELECT '1', '2', '3', '4' FROM INFORMATION_SCHEMA.SYSTEM_USERS")
    Immutable publicConstructorAutoMapping();

    @ConstructorArgs({ @Arg(column = "c1", javaType = String.class) })
    @Select("SELECT '1' as c1 FROM INFORMATION_SCHEMA.SYSTEM_USERS")
    Immutable privateConstructorMapping();

    @ConstructorArgs({
        // @formatter:off
        @Arg(column = "c1", javaType = String.class)
        ,@Arg(column = "c2", javaType = String.class)
        // @formatter:on
    })
    @Select("SELECT '1' as c1, '2' as c2 FROM INFORMATION_SCHEMA.SYSTEM_USERS")
    Immutable packagePrivateConstructorMapping();

    @ConstructorArgs({
        // @formatter:off
        @Arg(column = "c1", javaType = String.class)
        ,@Arg(column = "c2", javaType = String.class)
        ,@Arg(column = "c3", javaType = String.class)
        // @formatter:on
    })
    @Select("SELECT '1' as c1, '2' as c2, '3' as c3 FROM INFORMATION_SCHEMA.SYSTEM_USERS")
    Immutable protectedConstructorMapping();

    @ConstructorArgs({
        // @formatter:off
        @Arg(column = "c1", javaType = String.class)
        ,@Arg(column = "c2", javaType = String.class)
        ,@Arg(column = "c3", javaType = String.class)
        ,@Arg(column = "c4", javaType = String.class)
        // @formatter:on
    })
    @Select("SELECT '1' as c1, '2' as c2, '3' as c3, '4' as c4 FROM INFORMATION_SCHEMA.SYSTEM_USERS")
    Immutable publicConstructorMapping();

  }

  static class Params {
    private String privateField = "privateField";
    String packagePrivateField = "packagePrivateField";
    protected String protectedField = "protectedField";
    public String publicField = "publicField";

    private String getPrivateProperty() {
      return "privateProperty";
    }

    String getPackagePrivateProperty() {
      return "packagePrivateProperty";
    }

    protected String getProtectedProperty() {
      return "protectedProperty";
    }

    public String getPublicProperty() {
      return "publicProperty";
    }
  }

  @SuppressWarnings("unused")
  static class Bean {
    private String privateField;
    String packagePrivateField;
    protected String protectedField;
    public String publicField;
    private Map<String, String> properties = new HashMap<>();

    private void setPrivateProperty(String value) {
      properties.put("privateProperty", value);
    }

    void setPackagePrivateProperty(String value) {
      properties.put("packagePrivateProperty", value);
    }

    protected void setProtectedProperty(String value) {
      properties.put("protectedProperty", value);
    }

    public void setPublicProperty(String value) {
      properties.put("publicProperty", value);
    }
  }

  @SuppressWarnings("unused")
  static class Immutable {
    private Map<String, String> properties = new HashMap<>();

    private Immutable(String arg1) {
      properties.put("arg1", arg1);
    }

    Immutable(String arg1, String arg2) {
      properties.put("arg1", arg1);
      properties.put("arg2", arg2);
    }

    protected Immutable(String arg1, String arg2, String arg3) {
      properties.put("arg1", arg1);
      properties.put("arg2", arg2);
      properties.put("arg3", arg3);
    }

    public Immutable(String arg1, String arg2, String arg3, String arg4) {
      properties.put("arg1", arg1);
      properties.put("arg2", arg2);
      properties.put("arg3", arg3);
      properties.put("arg4", arg4);
    }

  }

}

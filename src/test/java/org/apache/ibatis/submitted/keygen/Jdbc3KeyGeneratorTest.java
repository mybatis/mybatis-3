/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.submitted.keygen;

import static com.googlecode.catchexception.apis.BDDCatchException.*;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author liuzh
 */
class Jdbc3KeyGeneratorTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/keygen/MapperConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/keygen/CreateDB.sql");
  }

  @Test
  void shouldAssignKeyToBean() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Country country = new Country("China", "CN");
        mapper.insertBean(country);
        assertNotNull(country.getId());
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssignKeyToBean_batch() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Country country1 = new Country("China", "CN");
        mapper.insertBean(country1);
        Country country2 = new Country("Canada", "CA");
        mapper.insertBean(country2);
        sqlSession.flushStatements();
        sqlSession.clearCache();
        assertNotNull(country1.getId());
        assertNotNull(country2.getId());
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssignKeyToNamedBean() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Country country = new Country("China", "CN");
        mapper.insertNamedBean(country);
        assertNotNull(country.getId());
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssignKeyToNamedBean_batch() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Country country1 = new Country("China", "CN");
        mapper.insertNamedBean(country1);
        Country country2 = new Country("Canada", "CA");
        mapper.insertNamedBean(country2);
        sqlSession.flushStatements();
        sqlSession.clearCache();
        assertNotNull(country1.getId());
        assertNotNull(country2.getId());
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssignKeyToNamedBean_keyPropertyWithParamName() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Country country = new Country("China", "CN");
        mapper.insertNamedBean_keyPropertyWithParamName(country);
        assertNotNull(country.getId());
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssignKeyToNamedBean_keyPropertyWithParamName_batch() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Country country1 = new Country("China", "CN");
        mapper.insertNamedBean_keyPropertyWithParamName(country1);
        Country country2 = new Country("Canada", "CA");
        mapper.insertNamedBean_keyPropertyWithParamName(country2);
        sqlSession.flushStatements();
        sqlSession.clearCache();
        assertNotNull(country1.getId());
        assertNotNull(country2.getId());
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssignKeysToList() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        List<Country> countries = new ArrayList<>();
        countries.add(new Country("China", "CN"));
        countries.add(new Country("United Kiongdom", "GB"));
        countries.add(new Country("United States of America", "US"));
        mapper.insertList(countries);
        for (Country country : countries) {
          assertNotNull(country.getId());
        }
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssignKeysToNamedList() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        List<Country> countries = new ArrayList<>();
        countries.add(new Country("China", "CN"));
        countries.add(new Country("United Kiongdom", "GB"));
        countries.add(new Country("United States of America", "US"));
        mapper.insertNamedList(countries);
        for (Country country : countries) {
          assertNotNull(country.getId());
        }
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssingKeysToCollection() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Set<Country> countries = new HashSet<>();
        countries.add(new Country("China", "CN"));
        countries.add(new Country("United Kiongdom", "GB"));
        mapper.insertSet(countries);
        for (Country country : countries) {
          assertNotNull(country.getId());
        }
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssingKeysToNamedCollection() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Set<Country> countries = new HashSet<>();
        countries.add(new Country("China", "CN"));
        countries.add(new Country("United Kiongdom", "GB"));
        mapper.insertNamedSet(countries);
        for (Country country : countries) {
          assertNotNull(country.getId());
        }
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssingKeysToArray() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Country[] countries = new Country[2];
        countries[0] = new Country("China", "CN");
        countries[1] = new Country("United Kiongdom", "GB");
        mapper.insertArray(countries);
        for (Country country : countries) {
          assertNotNull(country.getId());
        }
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssingKeysToNamedArray() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Country[] countries = new Country[2];
        countries[0] = new Country("China", "CN");
        countries[1] = new Country("United Kiongdom", "GB");
        mapper.insertNamedArray(countries);
        for (Country country : countries) {
          assertNotNull(country.getId());
        }
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssignKeyToBean_MultiParams() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Country country = new Country("China", "CN");
        mapper.insertMultiParams(country, 1);
        assertNotNull(country.getId());
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldFailIfKeyPropertyIsInvalid_NoParamName() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Country country = new Country("China", "CN");
        when(mapper).insertMultiParams_keyPropertyWithoutParamName(country, 1);
        then(caughtException()).isInstanceOf(PersistenceException.class).hasMessageContaining(
            "Could not determine which parameter to assign generated keys to. "
                + "Note that when there are multiple parameters, 'keyProperty' must include the parameter name (e.g. 'param.id'). "
                + "Specified key properties are [id] and available parameters are [");
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldFailIfKeyPropertyIsInvalid_WrongParamName() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Country country = new Country("China", "CN");
        when(mapper).insertMultiParams_keyPropertyWithWrongParamName(country, 1);
        then(caughtException()).isInstanceOf(PersistenceException.class).hasMessageContaining(
            "Could not find parameter 'bogus'. "
                + "Note that when there are multiple parameters, 'keyProperty' must include the parameter name (e.g. 'param.id'). "
                + "Specified key properties are [bogus.id] and available parameters are [");
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssignKeysToNamedList_MultiParams() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        List<Country> countries = new ArrayList<>();
        countries.add(new Country("China", "CN"));
        countries.add(new Country("United Kiongdom", "GB"));
        mapper.insertList_MultiParams(countries, 1);
        for (Country country : countries) {
          assertNotNull(country.getId());
        }
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssignKeysToNamedCollection_MultiParams() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Set<Country> countries = new HashSet<>();
        countries.add(new Country("China", "CN"));
        countries.add(new Country("United Kiongdom", "GB"));
        mapper.insertSet_MultiParams(countries, 1);
        for (Country country : countries) {
          assertNotNull(country.getId());
        }
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssignKeysToNamedArray_MultiParams() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Country[] countries = new Country[2];
        countries[0] = new Country("China", "CN");
        countries[1] = new Country("United Kiongdom", "GB");
        mapper.insertArray_MultiParams(countries, 1);
        for (Country country : countries) {
          assertNotNull(country.getId());
        }
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssignMultipleGeneratedKeysToABean() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Planet planet = new Planet();
        planet.setName("pluto");
        mapper.insertPlanet(planet);
        assertEquals("pluto-" + planet.getId(), planet.getCode());
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssignMultipleGeneratedKeysToBeans() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Planet planet1 = new Planet();
        planet1.setName("pluto");
        Planet planet2 = new Planet();
        planet2.setName("neptune");
        List<Planet> planets = Arrays.asList(planet1, planet2);
        mapper.insertPlanets(planets);
        assertEquals("pluto-" + planet1.getId(), planet1.getCode());
        assertEquals("neptune-" + planet2.getId(), planet2.getCode());
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssignMultipleGeneratedKeysToABean_MultiParams() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Planet planet = new Planet();
        planet.setName("pluto");
        mapper.insertPlanet_MultiParams(planet, 1);
        assertEquals("pluto-" + planet.getId(), planet.getCode());
      } finally {
        sqlSession.rollback();
      }
    }
  }
  @Test
  void shouldAssignMultipleGeneratedKeysToABean_MultiParams_batch() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Planet planet1 = new Planet();
        planet1.setName("pluto");
        mapper.insertPlanet_MultiParams(planet1, 1);
        Planet planet2 = new Planet();
        planet2.setName("neptune");
        mapper.insertPlanet_MultiParams(planet2, 1);
        sqlSession.flushStatements();
        sqlSession.clearCache();
        assertEquals("pluto-" + planet1.getId(), planet1.getCode());
        assertEquals("neptune-" + planet2.getId(), planet2.getCode());
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldAssignMultipleGeneratedKeysToBeans_MultiParams() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Planet planet1 = new Planet();
        planet1.setName("pluto");
        Planet planet2 = new Planet();
        planet2.setName("neptune");
        List<Planet> planets = Arrays.asList(planet1, planet2);
        mapper.insertPlanets_MultiParams(planets, 1);
        assertEquals("pluto-" + planet1.getId(), planet1.getCode());
        assertEquals("neptune-" + planet2.getId(), planet2.getCode());
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void assigningMultipleKeysToDifferentParams() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Planet planet = new Planet();
        planet.setName("pluto");
        Map<String, Object> map = new HashMap<>();
        mapper.insertAssignKeysToTwoParams(planet, map);
        assertNotNull(planet.getId());
        assertNotNull(map.get("code"));
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void assigningMultipleKeysToDifferentParams_batch() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);
        Planet planet1 = new Planet();
        planet1.setName("pluto");
        Map<String, Object> map1 = new HashMap<>();
        mapper.insertAssignKeysToTwoParams(planet1, map1);
        Planet planet2 = new Planet();
        planet2.setName("pluto");
        Map<String, Object> map2 = new HashMap<>();
        mapper.insertAssignKeysToTwoParams(planet2, map2);
        sqlSession.flushStatements();
        sqlSession.clearCache();
        assertNotNull(planet1.getId());
        assertNotNull(map1.get("code"));
        assertNotNull(planet2.getId());
        assertNotNull(map2.get("code"));
      } finally {
        sqlSession.rollback();
      }
    }
  }

  @Test
  void shouldErrorUndefineProperty() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        CountryMapper mapper = sqlSession.getMapper(CountryMapper.class);

        when(mapper).insertUndefineKeyProperty(new Country("China", "CN"));
        then(caughtException()).isInstanceOf(PersistenceException.class).hasMessageContaining(
                "### Error updating database.  Cause: org.apache.ibatis.executor.ExecutorException: Error getting generated key or setting result to parameter object. Cause: org.apache.ibatis.executor.ExecutorException: No setter found for the keyProperty 'country_id' in 'org.apache.ibatis.submitted.keygen.Country'.");
      } finally {
        sqlSession.rollback();
      }
    }
  }
}

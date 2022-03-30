/*
 *    Copyright 2009-2022 the original author or authors.
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.reflection.ParamNameResolver;

public interface CountryMapper {

  @Options(useGeneratedKeys = true, keyProperty = "id")
  @Insert({ "insert into country (countryname,countrycode) values (#{countryname},#{countrycode})" })
  int insertBean(Country country);

  @Options(useGeneratedKeys = true, keyProperty = "id")
  @Insert({ "insert into country (countryname,countrycode) values (#{country.countryname},#{country.countrycode})" })
  int insertNamedBean(@Param("country") Country country);

  @Options(useGeneratedKeys = true, keyProperty = "country.id")
  @Insert({ "insert into country (countryname,countrycode) values (#{country.countryname},#{country.countrycode})" })
  int insertNamedBean_keyPropertyWithParamName(@Param("country") Country country);

  int insertList(List<Country> countries);

  int insertNamedList(@Param("countries") List<Country> countries);

  int insertSet(Set<Country> countries);

  int insertNamedSet(@Param("countries") Set<Country> countries);

  int insertArray(Country[] countries);

  int insertNamedArray(@Param("countries") Country[] countries);

  @Options(useGeneratedKeys = true, keyProperty = "country.id")
  @Insert({ "insert into country (countryname,countrycode) values (#{country.countryname},#{country.countrycode})" })
  int insertMultiParams(@Param("country") Country country, @Param("someId") Integer someId);

  @Options(useGeneratedKeys = true, keyProperty = "id")
  @Insert({ "insert into country (countryname,countrycode) values (#{country.countryname},#{country.countrycode})" })
  int insertMultiParams_keyPropertyWithoutParamName(@Param("country") Country country, @Param("someId") Integer someId);

  @Options(useGeneratedKeys = true, keyProperty = "bogus.id")
  @Insert({ "insert into country (countryname,countrycode) values (#{country.countryname},#{country.countrycode})" })
  int insertMultiParams_keyPropertyWithWrongParamName(@Param("country") Country country,
      @Param("someId") Integer someId);

  int insertListAndSomeId(@Param("list") List<Country> countries, @Param("someId") Integer someId);

  int insertSetAndSomeId(@Param("collection") Set<Country> countries, @Param("someId") Integer someId);

  int insertArrayAndSomeId(@Param("array") Country[] countries, @Param("someId") Integer someId);

  int insertList_MultiParams(@Param("countries") List<Country> countries, @Param("someId") Integer someId);

  int insertSet_MultiParams(@Param("countries") Set<Country> countries, @Param("someId") Integer someId);

  int insertArray_MultiParams(@Param("countries") Country[] countries, @Param("someId") Integer someId);

  int insertUndefineKeyProperty(Country country);

  @Options(useGeneratedKeys = true, keyProperty = "id,code")
  @Insert({ "insert into planet (name) values (#{name})" })
  int insertPlanet(Planet planet);

  int insertPlanets(List<Planet> planets);

  @Options(useGeneratedKeys = true, keyProperty = "planet.id,planet.code")
  @Insert({ "insert into planet (name) values (#{planet.name})" })
  int insertPlanet_MultiParams(@Param("planet") Planet planet, @Param("someId") Integer someId);

  int insertPlanets_MultiParams(@Param("planets") List<Planet> planets, @Param("someId") Integer someId);

  @Options(useGeneratedKeys = true, keyProperty = "planet.id,map.code")
  @Insert({ "insert into planet (name) values (#{planet.name})" })
  int insertAssignKeysToTwoParams(@Param("planet") Planet planet, @Param("map") Map<String, Object> map);

  @Options(useGeneratedKeys = true, keyProperty = "id")
  @Insert({ "insert into country (countryname,countrycode) values ('a','A'), ('b', 'B')" })
  int tooManyGeneratedKeys(Country country);

  @Options(useGeneratedKeys = true, keyProperty = "country.id")
  @Insert({ "insert into country (countryname,countrycode) values ('a','A'), ('b', 'B')" })
  int tooManyGeneratedKeysParamMap(@Param("country") Country country, @Param("someId") Integer someId);

  int insertWeirdCountries(List<NpeCountry> list);

  // If the only parameter has a name 'param2', keyProperty must include the prefix 'param2.'.
  @Options(useGeneratedKeys = true, keyProperty = ParamNameResolver.GENERIC_NAME_PREFIX + "2.id")
  @Insert({ "insert into country (countryname,countrycode) values (#{param2.countryname},#{param2.countrycode})" })
  int singleParamWithATrickyName(@Param(ParamNameResolver.GENERIC_NAME_PREFIX + "2") Country country);

  @Options(useGeneratedKeys = true, keyProperty = "id")
  @Insert({ "insert into country (countryname,countrycode) values (#{countryname},#{countrycode})" })
  int insertMap(Map<String, Object> map);
}

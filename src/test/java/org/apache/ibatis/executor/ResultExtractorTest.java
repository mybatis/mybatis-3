/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ResultExtractorTest {

  private ResultExtractor resultExtractor;

  @Mock
  private Configuration configuration;
  @Mock
  private ObjectFactory objectFactory;

  @BeforeEach
  public void setUp() throws Exception {
    resultExtractor = new ResultExtractor(configuration, objectFactory);
  }

  @Test
  public void shouldExtractNullForNullTargetType() {
    final Object result = resultExtractor.extractObjectFromList(null, null);
    assertThat(result).isNull();
  }

  @Test
  public void shouldExtractList() {
    final List list = Arrays.asList(1, 2, 3);
    final Object result = resultExtractor.extractObjectFromList(list, List.class);
    assertThat(result).isInstanceOf(List.class);
    final List resultList = (List) result;
    assertThat(resultList).isEqualTo(list);
  }

  @Test
  public void shouldExtractArray() {
    final List list = Arrays.asList(1, 2, 3);
    final Object result = resultExtractor.extractObjectFromList(list, Integer[].class);
    assertThat(result).isInstanceOf(Integer[].class);
    final Integer[] resultArray = (Integer[]) result;
    assertThat(resultArray).isEqualTo(new Integer[]{1, 2, 3});
  }

  @Test
  public void shouldExtractSet() {
    final List list = Arrays.asList(1, 2, 3);
    final Class<Set> targetType = Set.class;
    final Set set = new HashSet();
    final MetaObject metaObject = mock(MetaObject.class);
    when(objectFactory.isCollection(targetType)).thenReturn(true);
    when(objectFactory.create(targetType)).thenReturn(set);
    when(configuration.newMetaObject(set)).thenReturn(metaObject);

    final Set result = (Set) resultExtractor.extractObjectFromList(list, targetType);
    assertThat(result).isSameAs(set);

    verify(metaObject).addAll(list);
  }

  @Test
  public void shouldExtractSingleObject() {
    final List list = Collections.singletonList("single object");
    assertThat((String) resultExtractor.extractObjectFromList(list, String.class)).isEqualTo("single object");
    assertThat((String) resultExtractor.extractObjectFromList(list, null)).isEqualTo("single object");
    assertThat((String) resultExtractor.extractObjectFromList(list, Integer.class)).isEqualTo("single object");
  }

  @Test
  public void shouldFailWhenMutipleItemsInList() {
    final List list = Arrays.asList("first object", "second object");
    Assertions.assertThrows(ExecutorException.class, () -> {
      resultExtractor.extractObjectFromList(list, String.class);
    });
  }
}

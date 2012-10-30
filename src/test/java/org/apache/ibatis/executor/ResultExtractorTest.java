package org.apache.ibatis.executor;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ResultExtractorTest {

  private ResultExtractor resultExtractor;

  @Mock
  private Configuration configuration;
  @Mock
  private ObjectFactory objectFactory;

  @Before
  public void setUp() throws Exception {
    resultExtractor = new ResultExtractor(configuration, objectFactory);
  }

  @Test
  public void shouldExtractNullForNullTargetType() {
    final Object result = resultExtractor.extractObjectFromList(null, null);
    assertThat(result, nullValue());
  }

  @Test
  public void shouldExtractList() {
    final List list = Arrays.asList(1, 2, 3);
    final Object result = resultExtractor.extractObjectFromList(list, List.class);
    assertThat(result, instanceOf(List.class));
    final List resultList = (List) result;
    assertThat(resultList, equalTo(list));
  }

  @Test
  public void shouldExtractArray() {
    final List list = Arrays.asList(1, 2, 3);
    final Object result = resultExtractor.extractObjectFromList(list, Integer[].class);
    assertThat(result, instanceOf(Integer[].class));
    final Integer[] resultArray = (Integer[]) result;
    assertThat(resultArray, equalTo(new Integer[]{1, 2, 3}));
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
    assertThat(result, sameInstance(set));

    verify(metaObject).addAll(list);
  }

  @Test
  public void shouldExtractSingleObject() {
    final List list = Collections.singletonList("single object");
    assertThat((String) resultExtractor.extractObjectFromList(list, String.class), equalTo("single object"));
    assertThat((String) resultExtractor.extractObjectFromList(list, null), equalTo("single object"));
    assertThat((String) resultExtractor.extractObjectFromList(list, Integer.class), equalTo("single object"));
  }

  @Test(expected = ExecutorException.class)
  public void shouldFailWhenMutipleItemsInList() {
    final List list = Arrays.asList("first object", "second object");
    assertThat((String) resultExtractor.extractObjectFromList(list, String.class), equalTo("single object"));
  }
}

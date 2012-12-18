package org.apache.ibatis.mapping;

import org.apache.ibatis.session.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResultMappingTest {
  @Mock
  private Configuration configuration;

  // Issue 697: Association with both a resultMap and a select attribute should throw exception
  @Test(expected = IllegalStateException.class)
  public void shouldThrowErrorWhenBothResultMapAndNestedSelectAreSet() {
    new ResultMapping.Builder(configuration, "prop")
        .nestedQueryId("nested query ID")
        .nestedResultMapId("nested resultMap")
        .build();
  }
  
  //Issue 4: column is mandatory on nested queries
  @Test(expected=IllegalStateException.class)
  public void shouldFailWithAMissingColumnInNetstedSelect() throws Exception {
    new ResultMapping.Builder(configuration, "prop")
    .nestedQueryId("nested query ID")
    .build();
  }

}

/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.submitted.results_id;

import org.apache.ibatis.session.Configuration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class IdConflictTest {

  @Rule
  public ExpectedException ex = ExpectedException.none();

  @Test
  public void shouldFailOnDuplicatedId() throws Exception {
    ex.expect(RuntimeException.class);
    ex.expectMessage("Result Maps collection already contains value for org.apache.ibatis.submitted.results_id.IdConflictMapper.userResult");

    Configuration configuration = new Configuration();
    configuration.addMapper(IdConflictMapper.class);
    configuration.getMappedStatements();
  }

}

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
package org.apache.ibatis.submitted.results_id;

import static com.googlecode.catchexception.apis.BDDCatchException.*;
import static org.assertj.core.api.BDDAssertions.then;

import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class IdConflictTest {

  @Test
  void shouldFailOnDuplicatedId() {
    Configuration configuration = new Configuration();
    when(() -> configuration.addMapper(IdConflictMapper.class));
    then(caughtException()).isInstanceOf(RuntimeException.class).hasMessage(
        "Result Maps collection already contains value for org.apache.ibatis.submitted.results_id.IdConflictMapper.userResult");
  }

}

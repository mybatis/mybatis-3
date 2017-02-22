/**
 * Copyright 2009-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.autoconstructor;

import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface Mapper {
  @Select("SELECT * FROM subject")
  List<PrimitiveSubject> getSubjects();

  @Select("SELECT * FROM subject WHERE id = #{id}")
  PrimitiveSubject getSubject(final int id);

  @Select("SELECT * FROM subject")
  List<WrapperSubject> getWrapperSubjects();
}

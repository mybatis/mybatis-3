/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.submitted.named_constructor_args.usesjava8;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.submitted.named_constructor_args.User;

public interface Mapper {

  @ConstructorArgs({ @Arg(column = "name", name = "name", javaType = String.class),
      @Arg(id = true, column = "id", name = "id", javaType = Integer.class) })
  @Select("select * from users where id = #{id}")
  User mapConstructorWithoutParamAnnos(@Param("id")Integer id);

  User mapConstructorWithoutParamAnnosXml(@Param("id")Integer id);

}

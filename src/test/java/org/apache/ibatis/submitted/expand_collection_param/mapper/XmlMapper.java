/**
 *    Copyright 2009-2020 the original author or authors.
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
package org.apache.ibatis.submitted.expand_collection_param.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.submitted.expand_collection_param.model.User;
import org.apache.ibatis.submitted.expand_collection_param.model.UserRole;

import java.util.List;

public interface XmlMapper {

  List<User> getUsers(@Param("id") Integer id, @Param("roles") List<UserRole> roles);

}

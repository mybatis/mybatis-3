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
package org.apache.ibatis.submitted.duplicate_statements;

import java.util.List;

import org.apache.ibatis.annotations.Select;

/**
 * This interface should fail when added to the configuration.  It has
 * a method with the same name, but different parameters, as a method
 * in the super interface
 *
 */
public interface AnnotatedMapperExtended extends AnnotatedMapper {

    @Select("select * from users")
    List<User> getAllUsers(int i);
}

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
package org.apache.ibatis.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation that specify result map names to use.
 *
 * <p>
 * <b>How to use:</b><br>
 * Mapper interface:
 *
 * <pre>
 * public interface UserMapper {
 *   &#064;Select("SELECT id, name FROM users WHERE id = #{id}")
 *   &#064;ResultMap("userMap")
 *   User selectById(int id);
 *
 *   &#064;Select("SELECT u.id, u.name FROM users u INNER JOIN users_email ue ON u.id = ue.id WHERE ue.email = #{email}")
 *   &#064;ResultMap("userMap")
 *   User selectByEmail(String email);
 * }
 * </pre>
 * Mapper XML:
 * <pre>{@code
 * <mapper namespace="com.example.mapper.UserMapper">
 *   <resultMap id="userMap" type="com.example.model.User">
 *     <id property="id" column="id" />
 *     <result property="name" column="name" />
 *     <association property="email" select="selectUserEmailById" column="id" fetchType="lazy"/>
 *   </resultMap>
 * </mapper>
 * }
 * </pre>
 *
 * @author Jeff Butler
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResultMap {
  /**
   * Returns result map names to use.
   *
   * @return result map names
   */
  String[] value();
}

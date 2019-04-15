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

package org.apache.ibatis.testcontainers;

import org.testcontainers.containers.PostgreSQLContainer;

public class PgContainer {

  public static String USER = "u";
  public static String PW = "p";
  public static String DRIVER = "org.postgresql.Driver";

  public static final PostgreSQLContainer<?> INSTANCE = initContainer();

  private static PostgreSQLContainer<?> initContainer() {
    @SuppressWarnings("resource")
    PostgreSQLContainer<?> container = new PostgreSQLContainer<>().withDatabaseName("mybatis_test").withUsername(USER)
        .withPassword(PW);
    container.start();
    return container;
  }

  private PgContainer() {
    super();
  }
}

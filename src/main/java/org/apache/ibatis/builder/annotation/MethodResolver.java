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
package org.apache.ibatis.builder.annotation;

import java.lang.reflect.Method;

/**
 * @author Eduardo Macarron
 */
public class MethodResolver {
  private final MapperAnnotationBuilder annotationBuilder;
  private final Method method;

  public MethodResolver(MapperAnnotationBuilder annotationBuilder, Method method) {
    this.annotationBuilder = annotationBuilder;
    this.method = method;
  }

  public void resolve() {
    annotationBuilder.parseStatement(method);
  }

}
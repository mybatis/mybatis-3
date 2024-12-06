/*
 *    Copyright 2009-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.executor.resultset;

import java.util.Objects;

import org.apache.ibatis.mapping.ResultMapping;

/**
 * A unique identifier for a pending constructor creation, prefix is used to distinguish between equal result maps for
 * different columns
 *
 * @author Willie Scholtz
 */
final class PendingCreationKey {
  private final String resultMapId;
  private final String constructorColumnPrefix;

  PendingCreationKey(ResultMapping constructorMapping) {
    this.resultMapId = constructorMapping.getNestedResultMapId();
    this.constructorColumnPrefix = constructorMapping.getColumnPrefix();
  }

  String getConstructorColumnPrefix() {
    return constructorColumnPrefix;
  }

  String getResultMapId() {
    return resultMapId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    PendingCreationKey that = (PendingCreationKey) o;
    return Objects.equals(resultMapId, that.resultMapId)
        && Objects.equals(constructorColumnPrefix, that.constructorColumnPrefix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resultMapId, constructorColumnPrefix);
  }

  @Override
  public String toString() {
    return "PendingCreationKey{" + "resultMapId='" + resultMapId + '\'' + ", constructorColumnPrefix='"
        + constructorColumnPrefix + '\'' + '}';
  }
}

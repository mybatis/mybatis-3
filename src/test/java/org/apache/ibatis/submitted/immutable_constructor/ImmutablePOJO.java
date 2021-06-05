/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.submitted.immutable_constructor;

import java.io.Serializable;

public class ImmutablePOJO implements Serializable {

  private static final long serialVersionUID = -7086198701202598455L;
  private final Integer immutableId;
  private final String immutableDescription;

  public ImmutablePOJO(Integer immutableId, String immutableDescription) {
    this.immutableId = immutableId;
    this.immutableDescription = immutableDescription;
  }

  public Integer getImmutableId() {
    return immutableId;
  }

  public String getImmutableDescription() {
    return immutableDescription;
  }

}

/*
 *    Copyright 2009-2026 the original author or authors.
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

package org.apache.ibatis.submitted.null_conversion_type_handler;

public enum BloodType {
  A(1), B(2), UNKNOWN(99);

  private final int value;

  private BloodType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public static BloodType of(final int value) {
    BloodType[] types = BloodType.values();
    for (BloodType type : types) {
      if (type.getValue() == value) {
        return type;
      }
    }
    return null;
  }
}

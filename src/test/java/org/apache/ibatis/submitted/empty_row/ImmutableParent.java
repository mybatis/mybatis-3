/*
 *    Copyright 2009-2023 the original author or authors.
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
package org.apache.ibatis.submitted.empty_row;

public class ImmutableParent {
  private final String col1;
  private final String col2;

  public ImmutableParent(String col1, String col2) {
    this.col1 = col1;
    this.col2 = col2;
  }

  public String getCol1() {
    return col1;
  }

  public String getCol2() {
    return col2;
  }
}

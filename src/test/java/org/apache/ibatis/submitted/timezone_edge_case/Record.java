/*
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
package org.apache.ibatis.submitted.timezone_edge_case;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Record {

  private Integer id;

  private LocalDateTime ts;
  private LocalDate d;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public LocalDateTime getTs() {
    return ts;
  }

  public void setTs(LocalDateTime ts) {
    this.ts = ts;
  }

  public LocalDate getD() {
    return d;
  }

  public void setD(LocalDate d) {
    this.d = d;
  }
}

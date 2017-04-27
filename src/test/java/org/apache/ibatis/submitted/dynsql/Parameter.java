/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.submitted.dynsql;

import java.util.List;

/*
 * @author Jeff Butler
 */
public class Parameter {
  private String schema;
  private List<Integer> ids;
  private boolean enabled;
  
  public String getFred() {
    // added this method to check for bug with DynamicContext
    // IBATIS-777
    throw new RuntimeException("This method should not be called.");
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public List<Integer> getIds() {
    return ids;
  }

  public void setIds(List<Integer> ids) {
    this.ids = ids;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}

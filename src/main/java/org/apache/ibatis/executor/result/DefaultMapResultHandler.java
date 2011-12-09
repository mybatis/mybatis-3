/*
 *    Copyright 2009-2011 The MyBatis Team
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
package org.apache.ibatis.executor.result;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

import java.util.HashMap;
import java.util.Map;

public class DefaultMapResultHandler implements ResultHandler {

  private final Map mappedResults = new HashMap();
  private final String mapKey;

  public DefaultMapResultHandler(String mapKey) {
    this.mapKey = mapKey;
  }

  public void handleResult(ResultContext context) {
    final Object value = context.getResultObject();
    final MetaObject mo = MetaObject.forObject(value);
    final Object key = mo.getValue(mapKey);
    mappedResults.put(key, value);
  }

  public Map getMappedResults() {
    return mappedResults;
  }
}

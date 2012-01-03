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

import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

import java.util.ArrayList;
import java.util.List;

public class DefaultResultHandler implements ResultHandler {

  private final List<Object> list;

  public DefaultResultHandler() {
    this(null);
  }

  @SuppressWarnings("unchecked")
  public DefaultResultHandler(Class<?> clazz) {
    if (clazz == null) {
      list = new ArrayList<Object>();
    } else {
      try {
        list = (List<Object>) clazz.newInstance();
      } catch (Exception e) {
        throw new ExecutorException("Failed to instantiate list result handler type.", e);
      }
    }
  }

  public void handleResult(ResultContext context) {
    list.add(context.getResultObject());
  }

  public List<Object> getResultList() {
    return list;
  }

}

/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.submitted.skip_on_empty;

import java.util.Set;

public class Request {
  private Set<Integer> idSet;

  public Set<Integer> getIdSet() {
    return idSet;
  }

  public void setIdSet(Set<Integer> idSet) {
    this.idSet = idSet;
  }

  public static class RequestHolder{
    private Request ref;
    private String somethingElse;

    public Request getRef() {
      return ref;
    }

    public void setRef(Request ref) {
      this.ref = ref;
    }

    public String getSomethingElse() {
      return somethingElse;
    }

    public void setSomethingElse(String somethingElse) {
      this.somethingElse = somethingElse;
    }
  }
}

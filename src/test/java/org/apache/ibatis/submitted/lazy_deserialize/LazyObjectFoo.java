/*
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
package org.apache.ibatis.submitted.lazy_deserialize;

import java.io.Serializable;

/**
 * @since 2011-04-06T10:57:30+0200
 * @author Franta Mejta
 */
public class LazyObjectFoo implements Serializable {

  private static final long serialVersionUID = 1L;
  private Integer id;
  private LazyObjectBar lazyObjectBar;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public LazyObjectBar getLazyObjectBar() {
    return this.lazyObjectBar;
  }

  public void setLazyObjectBar(final LazyObjectBar lazyObjectBar) {
    this.lazyObjectBar = lazyObjectBar;
  }

}

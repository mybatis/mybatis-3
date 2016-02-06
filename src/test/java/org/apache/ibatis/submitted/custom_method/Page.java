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
package org.apache.ibatis.submitted.custom_method;

import org.apache.ibatis.session.RowBounds;

import java.io.Serializable;
import java.util.List;

public class Page<E> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<E> content;
    private final long total;
    private final RowBounds rowBounds;

    public Page(List<E> content, long total, RowBounds rowBounds) {
        this.content = content;
        this.total = total;
        this.rowBounds = rowBounds;
    }

    public List<E> getContent() {
        return content;
    }

    public long getTotal() {
        return total;
    }

    public RowBounds getRowBounds() {
        return rowBounds;
    }

}

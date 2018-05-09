/**
 * Copyright 2009-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.reflection.property;

import java.util.Iterator;

/**
 * @author Clinton Begin
 */
public class PropertyTokenizer implements Iterator<PropertyTokenizer> {
    /**
     * 当前表达式的名称
     */
    private String name;
    /**
     * 当前表达式的索引名称
     */
    private final String indexedName;
    /**
     * 索引下标
     */
    private String index;
    /**
     * 子表达式
     */
    private final String children;

    public PropertyTokenizer(String fullname) {
        // fullname 例如： orders[0].items[0].name
        int delim = fullname.indexOf('.');
        if (delim > -1) {
            // orders[0]
            name = fullname.substring(0, delim);
            // items[0].name
            children = fullname.substring(delim + 1);
        } else {
            name = fullname;
            children = null;
        }
        indexedName = name;
        delim = name.indexOf('[');
        if (delim > -1) {
            // 相当于上面例子的orders里面的0
            index = name.substring(delim + 1, name.length() - 1);
            // 相当于上面例子的orders
            name = name.substring(0, delim);
        }
    }

    public String getName() {
        return name;
    }

    public String getIndex() {
        return index;
    }

    public String getIndexedName() {
        return indexedName;
    }

    public String getChildren() {
        return children;
    }

    @Override
    public boolean hasNext() {
        return children != null;
    }

    @Override
    public PropertyTokenizer next() {
        // next创建一个新的PropertyTokenizer解析器，解析children
        return new PropertyTokenizer(children);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
    }
}

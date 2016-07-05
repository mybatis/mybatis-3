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
package org.apache.ibatis.submitted.nested_entity_hashing;

import java.util.Collection;

public class Parent {

    private Integer id;
    private Child child;
    private Collection<Child> children;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Child getChild() {
        return child;
    }

    public void setChild(Child child) {
        this.child = child;
    }

    public Collection<Child> getChildren() {
        return children;
    }

    public void setChildren(Collection<Child> children) {
        this.children = children;
    }

}

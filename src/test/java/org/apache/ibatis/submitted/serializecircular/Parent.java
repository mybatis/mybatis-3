/*
 *    Copyright 2012 the original author or authors.
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
package org.apache.ibatis.submitted.serializecircular;

import java.io.Serializable;
import java.util.List;

public class Parent implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer id;
	private List<Child> children;
	private Person person;

	public Integer getId() {
		return id;
	}
	public void setId(Integer aId) {
		id = aId;
	}
	public List<Child> getChildren() {
		return children;
	}
	public void setChildren(List<Child> aChildren) {
		children = aChildren;
	}
	public Person getPerson() {
		return person;
	}
	public void setPerson(Person aPerson) {
		person = aPerson;
	}
}

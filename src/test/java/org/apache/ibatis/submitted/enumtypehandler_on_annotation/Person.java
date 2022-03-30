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
package org.apache.ibatis.submitted.enumtypehandler_on_annotation;

/**
 * @since #444
 * @author Kazuki Shimizu
 */
public class Person {

    enum PersonType {
        PERSON,
        EMPLOYEE
    }

    private Integer id;
    private String firstName;
    private String lastName;
    private PersonType personType;

    public Person() {
    }

    public Person(Integer id, String firstName, String lastName, PersonType personType) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.personType = personType;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public PersonType getPersonType() {
        return personType;
    }
    public void setPersonType(PersonType personType) {
        this.personType = personType;
    }

}

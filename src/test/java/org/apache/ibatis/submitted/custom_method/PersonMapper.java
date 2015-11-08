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

import org.apache.ibatis.annotations.CustomMethod;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * Mapper interface which defined some custom methods.
 * @author Kazuki Shimizu
 */
public interface PersonMapper {

    // Mapper methods
    long countAll();
    List<Person> findList(RowBounds rowBounds);

    // Custom methods
    @CustomMethod
    Page<Person> findPage();

    @CustomMethod
    Page<Person> findPage(RowBounds rowBounds);

    @CustomMethod
    Page<Person> findPageByName(String name, RowBounds rowBounds);

    @CustomMethod(type = PersonCollector.class, method = "collect")
    long collectPage(RowBounds rowBounds, ResultHandler resultHandler);

    @CustomMethod
    Page<Person> findPageDelegatingMapperInterface(RowBounds rowBounds);

    @CustomMethod
    long findPageNotImplementCustomMethod();

    @CustomMethod
    long findPageThrowRuntimeException();

    @CustomMethod
    long findPageThrowError();

    @CustomMethod
    long findPageThrowException();

}

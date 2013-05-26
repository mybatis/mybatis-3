/*
 *    Copyright 2009-2012 the original author or authors.
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
package org.apache.ibatis.submitted.selectkey;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectKey;

public interface AnnotatedMapper {

    @Insert("insert into table2 (name) values(#{name})")
    @SelectKey(statement="call identity()", keyProperty="nameId", before=false, resultType=int.class)
    int insertTable2(Name name);
    
    @Insert("insert into table2 (name) values(#{name})")
    @Options(useGeneratedKeys=true, keyProperty="nameId,generatedName", keyColumn="ID,NAME_FRED")
    int insertTable2WithOptions(Name name);
    
    @Insert("insert into table3 (id, name) values(#{nameId}, #{name})")
    @SelectKey(statement="call next value for TestSequence", keyProperty="nameId", before=true, resultType=int.class)
    int insertTable3(Name name);

    @InsertProvider(type=SqlProvider.class,method="insertTable3_2")
    @SelectKey(statement="call next value for TestSequence", keyProperty="nameId", before=true, resultType=int.class)
    int insertTable3_2(Name name);
}

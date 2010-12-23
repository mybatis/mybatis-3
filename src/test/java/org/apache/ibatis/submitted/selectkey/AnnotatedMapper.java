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
    @Options(useGeneratedKeys=true, keyProperty="nameId")
    int insertTable2WithOptions(Name name);
    
    @Insert("insert into table3 (id, name) values(#{nameId}, #{name})")
    @SelectKey(statement="call next value for TestSequence", keyProperty="nameId", before=true, resultType=int.class)
    int insertTable3(Name name);

    @InsertProvider(type=SqlProvider.class,method="insertTable3_2")
    @SelectKey(statement="call next value for TestSequence", keyProperty="nameId", before=true, resultType=int.class)
    int insertTable3_2(Name name);
}

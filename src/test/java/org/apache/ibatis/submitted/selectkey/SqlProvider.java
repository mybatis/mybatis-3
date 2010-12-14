package org.apache.ibatis.submitted.selectkey;

public class SqlProvider {

    public String insertTable3_2(Name name) {
        return "insert into table3 (id, name) values(#{nameId}, #{name})";
    }
}

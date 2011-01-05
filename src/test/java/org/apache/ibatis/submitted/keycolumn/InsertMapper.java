package org.apache.ibatis.submitted.keycolumn;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

public interface InsertMapper {

    @Insert({
        "insert into mbtest.test_identity",
        "(first_name, last_name)",
        "values(#{firstName,jdbcType=VARCHAR}, #{lastName,jdbcType=VARCHAR})"
    })
    @Options(keyProperty="id", useGeneratedKeys=true, keyColumn="name_id")
    int insertNameAnnotated(Name name);

    int insertNameMapped(Name name);
}

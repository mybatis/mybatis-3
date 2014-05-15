package org.apache.ibatis.submitted.one_parameterprovider;

import org.apache.ibatis.annotations.*;

import java.util.Date;

public interface AccountMapper {
  @Select("select * from account where id = #{1}")
  @Results({
      @Result(column = "id", property = "id"),
      @Result(column = "name", property = "name"),
      @Result(column = "company_id", property = "accountant", javaType = Accountant.class,
        one = @One(select = "org.apache.ibatis.submitted.one_parameterprovider.AccountantMapper.selectByCompany",
            parameterProvider = @OneParameterProvider(
                type = DateParameterProvider.class, method = "getParametersWithDate"
            )))
  })
  Account select(Date date, Long id);
}

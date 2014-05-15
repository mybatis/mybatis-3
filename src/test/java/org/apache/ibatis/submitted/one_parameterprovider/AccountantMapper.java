package org.apache.ibatis.submitted.one_parameterprovider;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

public interface AccountantMapper {
  @Select("select * from accountant where company = #{1} and start_dt <= #{0} and end_dt > #{0}")
  @Results({
    @Result(column = "id", property = "id"),
      @Result(column = "company", property = "companyId"),
      @Result(column = "firstName", property = "firstName"),
      @Result(column = "lastName", property = "lastName"),
      @Result(column = "start_dt", property = "startDate"),
      @Result(column = "end_dt", property = "endDate")
  })
  Accountant selectByCompany(Date date, Long id);
}

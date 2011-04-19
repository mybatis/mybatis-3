package org.apache.ibatis.submitted.sptests;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.mapping.StatementType;

public interface SPMapper {
    // XML based
    Object adderAsSelect(Parameter parameter);
    void adderAsUpdate(Parameter parameter);
    void adderWithParameterMap(Map<String, Object> parameter);
    Name getName(Integer id);
    List<Name> getNames(Map<String, Object> parms);
    List<Name> getNamesWithArray(Map<String, Object> parms);
    List<List<?>> getNamesAndItems();
    
    // annotated
    @Select({"{call sptest.adder(",
      "#{addend1,jdbcType=INTEGER,mode=IN},",
      "#{addend2,jdbcType=INTEGER,mode=IN},",
      "#{sum,jdbcType=INTEGER,mode=OUT})}"
    })
    @Options(statementType=StatementType.CALLABLE)
    Object adderAsSelectAnnotated(Parameter parameter);
    
    @Update({"{call sptest.adder(",
      "#{addend1,jdbcType=INTEGER,mode=IN},",
      "#{addend2,jdbcType=INTEGER,mode=IN},",
      "#{sum,jdbcType=INTEGER,mode=OUT})}"
    })
    @Options(statementType=StatementType.CALLABLE)
    void adderAsUpdateAnnotated(Parameter parameter);

    @Select("{call sptest.getname(#{id,jdbcType=INTEGER,mode=IN})}")
    @Results({
        @Result(column="ID", property="id"),
        @Result(column="FIRST_NAME", property="firstName"),
        @Result(column="LAST_NAME", property="lastName")
    })
    @Options(statementType=StatementType.CALLABLE)
    Name getNameAnnotated(Integer id);

    @Select("{call sptest.getname(#{id,jdbcType=INTEGER,mode=IN})}")
    @ResultMap("nameResult")
    @Options(statementType=StatementType.CALLABLE)
    Name getNameAnnotatedWithXMLResultMap(Integer id);
    
    @Select({"{call sptest.getnames(",
      "#{lowestId,jdbcType=INTEGER,mode=IN},",
      "#{totalRows,jdbcType=INTEGER,mode=OUT})}"
    })
    @Results({
      @Result(column="ID", property="id"),
      @Result(column="FIRST_NAME", property="firstName"),
      @Result(column="LAST_NAME", property="lastName")
    })
    @Options(statementType=StatementType.CALLABLE)
    List<Name> getNamesAnnotated(Map<String, Object> parms);
    
    @Select({"{call sptest.getnames(",
      "#{lowestId,jdbcType=INTEGER,mode=IN},",
      "#{totalRows,jdbcType=INTEGER,mode=OUT})}"
    })
    @ResultMap("nameResult")
    @Options(statementType=StatementType.CALLABLE)
    List<Name> getNamesAnnotatedWithXMLResultMap(Map<String, Object> parms);

    @Select({"{call sptest.arraytest(",
      "#{ids,mode=IN,jdbcType=ARRAY,typeHandler=org.apache.ibatis.submitted.sptests.ArrayTypeHandler},",
      "#{requestedRows,jdbcType=INTEGER,mode=OUT},",
      "#{returnedIds,mode=OUT,jdbcType=ARRAY,typeHandler=org.apache.ibatis.submitted.sptests.ArrayTypeHandler})}"
    })
    @Results({
      @Result(column="ID", property="id"),
      @Result(column="FIRST_NAME", property="firstName"),
      @Result(column="LAST_NAME", property="lastName")
    })
    @Options(statementType=StatementType.CALLABLE)
    List<Name> getNamesWithArrayAnnotated(Map<String, Object> parms);
    
    @Select({"{call sptest.arraytest(",
      "#{ids,mode=IN,jdbcType=ARRAY,typeHandler=org.apache.ibatis.submitted.sptests.ArrayTypeHandler},",
      "#{requestedRows,jdbcType=INTEGER,mode=OUT},",
      "#{returnedIds,mode=OUT,jdbcType=ARRAY,typeHandler=org.apache.ibatis.submitted.sptests.ArrayTypeHandler})}"
    })
    @ResultMap("nameResult")
    @Options(statementType=StatementType.CALLABLE)
    List<Name> getNamesWithArrayAnnotatedWithXMLResultMap(Map<String, Object> parms);
    
    @Select("{call sptest.getnamesanditems()}")
    @ResultMap("nameResult,itemResult")
    @Options(statementType=StatementType.CALLABLE)
    List<List<?>> getNamesAndItemsAnnotatedWithXMLResultMap();
}

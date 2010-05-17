package org.apache.ibatis.submitted.primitive_result_type;

import java.math.BigDecimal;
import java.util.List;

public interface ProductMapper {

  List<Integer> selectProductCodes();

  List<Long> selectProductCodesL();

  List<BigDecimal> selectProductCodesB();

}

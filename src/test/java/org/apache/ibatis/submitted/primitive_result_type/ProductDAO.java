package org.apache.ibatis.submitted.primitive_result_type;

import org.apache.ibatis.session.SqlSession;

import java.math.BigDecimal;
import java.util.List;

public class ProductDAO {

  public static List<Integer> selectProductCodes() {
    SqlSession session = IbatisConfig.getSession();
    try {
      ProductMapper productMapper = session.getMapper(ProductMapper.class);
      return productMapper.selectProductCodes();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      session.close();
    }
  }

  public static List<Long> selectProductCodesL() {
    SqlSession session = IbatisConfig.getSession();
    try {
      ProductMapper productMapper = session.getMapper(ProductMapper.class);
      return productMapper.selectProductCodesL();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      session.close();
    }
  }

  public static List<BigDecimal> selectProductCodesB() {
    SqlSession session = IbatisConfig.getSession();
    try {
      ProductMapper productMapper = session.getMapper(ProductMapper.class);
      return productMapper.selectProductCodesB();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      session.close();
    }
  }


}
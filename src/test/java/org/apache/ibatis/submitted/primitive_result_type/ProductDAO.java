/*
 *    Copyright 2009-2020 the original author or authors.
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
package org.apache.ibatis.submitted.primitive_result_type;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

public class ProductDAO {

  public static List<Integer> selectProductCodes() {
    try (SqlSession session = IbatisConfig.getSession()) {
      ProductMapper productMapper = session.getMapper(ProductMapper.class);
      return productMapper.selectProductCodes();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static List<Long> selectProductCodesL() {
    try (SqlSession session = IbatisConfig.getSession()) {
      ProductMapper productMapper = session.getMapper(ProductMapper.class);
      return productMapper.selectProductCodesL();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static List<BigDecimal> selectProductCodesB() {
    try (SqlSession session = IbatisConfig.getSession()) {
      ProductMapper productMapper = session.getMapper(ProductMapper.class);
      return productMapper.selectProductCodesB();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static List<Product> selectAllProducts() {
    try (SqlSession session = IbatisConfig.getSession()) {
      ProductMapper productMapper = session.getMapper(ProductMapper.class);
      return productMapper.selectAllProducts();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}

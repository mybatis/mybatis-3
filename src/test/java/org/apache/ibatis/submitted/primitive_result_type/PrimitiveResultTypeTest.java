package org.apache.ibatis.submitted.primitive_result_type;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public class PrimitiveResultTypeTest {

  @BeforeClass
  public static void setup() throws Exception {
    SqlSession session = IbatisConfig.getSession();
    Connection conn = session.getConnection();
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.setErrorLogWriter(null);
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/primitive_result_type/create.sql");
    runner.runScript(reader);
  }

  @Test
  public void shouldReturnProperPrimitiveType() {
    List codes = ProductDAO.selectProductCodes();
    for (Object code : codes) {
      assertTrue(code instanceof Integer);
    }
    List lcodes = ProductDAO.selectProductCodesL();
    for (Object lcode : lcodes) {
      assertTrue(!(lcode instanceof Integer));
    }
    List bcodes = ProductDAO.selectProductCodesB();
    for (Object bcode : bcodes) {
      assertTrue(bcode instanceof BigDecimal);
    }
  }

}

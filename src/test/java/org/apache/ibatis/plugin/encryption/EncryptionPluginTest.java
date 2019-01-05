package org.apache.ibatis.plugin.encryption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author SoungRyoul Kim Thank my mentor Ikchan Sim who taught me.
 */
public class EncryptionPluginTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void createDB() throws SQLException, ClassNotFoundException {
    Class.forName("org.hsqldb.jdbc.JDBCDriver");
    initDatabase();
  }

  private static Connection getConnection() throws SQLException {
    return DriverManager.getConnection("jdbc:hsqldb:mem:plugin_test_db", "sa", "");

  }

  private static void initDatabase() throws SQLException {

    try (Connection connection = getConnection(); Statement statement = connection
        .createStatement()) {

      statement.execute(
          "CREATE TABLE customer (id VARCHAR(100) NOT NULL, name VARCHAR(100) NOT NULL,reg_date DATETIME ,"
              + " description VARCHAR(100) NOT NULL, mileage INT NOT NULL, email VARCHAR(100) NOT NULL, PRIMARY KEY (id))");
      connection.commit();
    }
  }

  @Before
  public void createMybatisConfig() {
    try {
      String resource = "org/apache/ibatis/plugin/encryption/config-mybatis-plugin-test.xml";
      Reader reader = Resources.getResourceAsReader(resource);
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testEncryptionPlugedSuccess() {

    SqlSession sqlSession = sqlSessionFactory.openSession();

    try {
      CustomerMapper customerMapper = sqlSession.getMapper(CustomerMapper.class);
      String id = UUID.randomUUID().toString();
      String email = "KimSoungRyoul@gmail.com";
      String name = "SoungRyoul Kim";

      Customer customer = new Customer();
      customer.setId(id);
      customer.setEmail(email);
      customer.setName(name);
      customer.setRegDate(new Date());
      customer.setMileage(1500);
      customer.setDescription("this is encryption not required content");
      customerMapper.insert(customer);

      String id2 = UUID.randomUUID().toString();
      Customer customer2 = new Customer();
      customer2.setId(id2);
      customer2.setEmail(email + "2");
      customer2.setName(name + "2");
      customer2.setRegDate(new Date());
      customer2.setMileage(15002);
      customer2.setDescription("this is encryption not required content2");
      customerMapper.insert(customer2);

      List<Customer> selectedCustomerList = customerMapper.selectAll();

      for (Customer selectedCustomer : selectedCustomerList) {
        if (selectedCustomer.getId().equals(id)) {
          assertEquals(email, selectedCustomer.getEmail());
          assertNotEquals(name, selectedCustomer.getName());
        } else {
          assertEquals(email + "2", selectedCustomer.getEmail());
          assertNotEquals(name + "2", selectedCustomer.getName());
        }
      }
      sqlSession.rollback();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      sqlSession.close();
    }

  }


}

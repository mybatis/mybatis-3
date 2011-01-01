package org.apache.ibatis.submitted.refcursor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class contains tests for refcursors.  The tests require a
 * local install of PostgreSQL and cannot be run as a part of the normal
 * MyBatis build unless PostreSQL is setup on the build machine as 
 * described in setupdb.txt
 * 
 * If PostgreSQL is setup as described in setupdb.txt, then remove
 * the @Ignore annotation to enable the tests.
 * 
 * @author Jeff Butler
 *
 */
@Ignore("See setupdb.txt for instructions on how to run the tests in this class")
public class RefCursorTest {
    @SuppressWarnings("unchecked")
    @Test
    public void testRefCursor1() throws IOException {
        Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/refcursor/MapperConfig.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            OrdersMapper mapper = sqlSession.getMapper(OrdersMapper.class);
            Map<String, Object> parameter = new HashMap<String, Object>();
            parameter.put("orderId", 1);
            mapper.getOrder1(parameter);
            
            assertNotNull(parameter.get("order"));
            List<Order> orders = (List<Order>) parameter.get("order");
            assertEquals(1, orders.size());
            Order order = orders.get(0);
            assertEquals(3, order.getDetailLines().size());
        } finally {
            sqlSession.close();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRefCursor2() throws IOException {
        Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/refcursor/MapperConfig.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            OrdersMapper mapper = sqlSession.getMapper(OrdersMapper.class);
            Map<String, Object> parameter = new HashMap<String, Object>();
            parameter.put("orderId", 1);
            mapper.getOrder2(parameter);
            
            assertNotNull(parameter.get("order"));
            List<Order> orders = (List<Order>) parameter.get("order");
            assertEquals(1, orders.size());
            Order order = orders.get(0);
            assertEquals(3, order.getDetailLines().size());
        } finally {
            sqlSession.close();
        }
    }
}

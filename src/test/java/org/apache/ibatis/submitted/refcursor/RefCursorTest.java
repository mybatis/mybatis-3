/*
 *    Copyright 2009-2012 the original author or authors.
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

/*
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

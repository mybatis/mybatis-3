package org.apache.ibatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;

public class MybatisTest {
    public static void main(String[] args) throws Exception {
        InputStream inputStream = Resources.getResourceAsStream("mybatis.xml");
        // build 方法中主要是通过加载的Resource（xml）文件进行遍历解析，并存在configuration当中
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            Object o = sqlSession.selectOne("org.apache.ibatis.mapper.UserMapper.selectById", 1);
            System.out.println("one = " + o);
            System.out.println("MybatisTest.main+++++++++++++++++++++++++++++++++++++++++++++++++");
            Object t = sqlSession.selectOne("org.apache.ibatis.mapper.UserMapper.selectById", 1);
            System.out.println("two = " + t);
        } finally {
            sqlSession.close();
        }
    }
}

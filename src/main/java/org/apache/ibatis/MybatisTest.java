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
            // 有关一级缓存,开启sqlSession后其属性里拿了一个执行器的引用,执行器里有一个localCache的map集合,每次查询数据库完毕后会默认的缓存结果集在该map中 下次直接获取 ,返回结果为空再去查询数据库
            Object t = sqlSession.selectOne("org.apache.ibatis.mapper.UserMapper.selectById", 1);
            System.out.println("two = " + t);
        } finally {
            sqlSession.close();
        }
        SqlSession sqlSession1 = sqlSessionFactory.openSession();
        Object o1 = sqlSession1.selectOne("selectById", 1);
        System.out.println("one = " + o1);
    }
}

package org.apache.ibatis;

import org.apache.ibatis.entity.User;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapper.UserMapper;
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
        try {
            Object o1 = sqlSession1.selectOne("selectById", 1);
            System.out.println("one = " + o1);
        } finally {
            sqlSession1.close();
        }
        SqlSession sqlSession2 = sqlSessionFactory.openSession();
        // 在xml解析的时候  通过namespace获取到对应的接口，然后保存到configuration中 可以通过getMapper方法获取到该mapper的代理对象(下面的语句对应为UserMapper其实是mapper的代理对象)
        UserMapper mapper = sqlSession2.getMapper(UserMapper.class);
        // mapper执行该mapper的代理对象其实执行MapperProxy里的invoke方法(mapperMethod.execute(sqlSession, args)),然后执行selectOne方法， 与上面的selectOne执行相同并获取对应的值。
        User user = mapper.selectById(1);
        System.out.println("user = " + user);

    }
}

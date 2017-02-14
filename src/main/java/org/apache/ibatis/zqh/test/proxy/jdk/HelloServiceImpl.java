package org.apache.ibatis.zqh.test.proxy.jdk;

/**
 * Created by zqh on 2017/2/14.
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public void sayHello(String name) {
        System.err.println("hello" + name);
    }

    @Override
    public void sayHelloWorld(String name) {
        System.err.println("xxoo" + name);
    }
}

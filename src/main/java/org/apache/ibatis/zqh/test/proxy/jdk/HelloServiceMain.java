package org.apache.ibatis.zqh.test.proxy.jdk;

/**
 * Created by zqh on 2017/2/14.
 */
public class HelloServiceMain {


    public static void main(String[] args) {
        //要代理的真实对象
        HelloService helloService = new HelloServiceImpl();
        HelloServiceProxy helloHander = new HelloServiceProxy();

        //代理对象关联到hander
        HelloService proxy = (HelloService) helloHander.bind(helloService);
        proxy.sayHelloWorld("欧阳婷");
    }

}

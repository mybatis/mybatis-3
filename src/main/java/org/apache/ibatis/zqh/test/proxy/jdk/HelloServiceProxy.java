package org.apache.ibatis.zqh.test.proxy.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by zqh on 2017/2/14.
 * 每一个动态代理类必须实现InvocationHandler接口
 */
public class HelloServiceProxy implements InvocationHandler {

    /**
     * real object
     */
    private Object target;


    /**
     * 绑定委托对象并返回一个代理类
     * 这里通过this将hander（HelloServiceProxy）绑定到代理对象上，并返回代理对象
     *
     * @param target
     * @return
     */
    public Object bind(Object target) {

        this.target = target;

        /**
         * 取得代理对象
         * 并且每个代理类的实例都关联到了一个handler
         * 这里将这个代理对象关联到了上方的 InvocationHandler 这个对象上
         *
         * 通过Proxy的newProxyInstance方法来创建我们的代理对象，我们来看看其三个参数
         * 第一个参数 target.getClass().getClassLoader() ，我们这里使用target这个类的ClassLoader对象来加载我们的代理对象
         * 第二个参数target.getClass().getInterfaces()，我们这里为代理对象提供的接口是真实对象所实行的接口，表示我要代理的是该真实对象，这样我就能调用这组接口中的方法了
         * 第三个参数target， 我们这里将这个代理对象关联到了上方的 HelloServiceProxy 这个对象上
         *
         */
        return Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(), this);

    }

    /**
     * 通过代理对象调用方法首先进入这个方法
     *
     * @param proxy  代理对象
     * @param method 被调用方法
     * @param args   方法的参数
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        /**
         * 在代理真实对象之前，添加一些自己的操作
         */
        System.err.println("##我是JDK动态代理##");

        Object result = null;

        System.err.println("我准备说hello。");

        /**
         *代理对象调用真实对象的方法，会自动地跳转到代理对象关联的handler对象的invoke方法
         */
        result = method.invoke(target, args);


        /**
         * 在代理真实对象之后，添加一些自己的操作
         */
        System.err.println("我说过hello了。");

        return result;
    }
}

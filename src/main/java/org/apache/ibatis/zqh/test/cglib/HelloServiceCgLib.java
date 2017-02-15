package org.apache.ibatis.zqh.test.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * Created by zqh on 2017/2/14.
 */
public class HelloServiceCgLib implements MethodInterceptor {

    private Object target;

    //创建代理对象
    public Object getInstance(Object target) {
        this.target = target;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(this.target.getClass());

        //回调方法
        enhancer.setCallback(this);
        //创建代理对象
        return enhancer.create();
    }


    //回调方法
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

        //反射前方法调用
        System.err.println("##我是CFLIB的动态代理##");


        System.err.println("我准备说hello了。");

        //反射后方法调用
        Object retunrObj = methodProxy.invokeSuper(o, objects);

        System.err.println("我说过hello了");
        return retunrObj;
    }
}

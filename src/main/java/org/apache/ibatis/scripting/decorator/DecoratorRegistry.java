/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.scripting.decorator;

import org.apache.ibatis.registry.GenericRegistry;
import org.apache.ibatis.registry.GenericRegistry.NamedRegistry;
import org.apache.ibatis.scripting.decorator.bind.BindFunction;
import org.apache.ibatis.scripting.decorator.bind.BindFunctionFactory;
import org.apache.ibatis.scripting.decorator.bind.impl.BaseBindFunctionFactory;
import org.apache.ibatis.session.Configuration;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author andyslin
 */
public class DecoratorRegistry {

  /**
   * Bind函数注册器
   */
  private static final NamedRegistry<BindFunction> bindFunctionNamedRegistry = GenericRegistry.getNamedRegistry(BindFunction.class);

  static {
    registerBindFunctionFactory(new BaseBindFunctionFactory());
  }

  //======Bind函数=========
  //////////////////////////

  /**
   * 执行Bind函数
   *
   * @param configuration
   * @param node
   * @return
   */
  public static void evalBindFunction(Configuration configuration, Node node) {
    Element element = (Element) node;
    String name = element.getAttribute("name").substring(1);
    int index = name.indexOf(".");
    String subName = null;
    if (-1 != index) {
      subName = name.substring(index + 1);
      name = name.substring(0, index);
    }
    BindFunction bindFunction = bindFunctionNamedRegistry.get(name);
    if (null == bindFunction) {
      throw new RuntimeException("not found bind-function [name=" + name + "]");
    }
    String value = element.getAttribute("value");
    bindFunction.eval(configuration, element, subName, value);
  }

  /**
   * 注册Bind函数
   *
   * @param bindFunctions
   */
  public static void registerBindFunction(BindFunction... bindFunctions) {
    bindFunctionNamedRegistry.register(bindFunctions);
  }

  /**
   * 注册Bind函数工厂
   *
   * @param bindFunctionFactories
   */
  public static void registerBindFunctionFactory(BindFunctionFactory... bindFunctionFactories) {
    if (null != bindFunctionFactories) {
      Arrays.stream(bindFunctionFactories)
        .filter(Objects::nonNull)
        .map(BindFunctionFactory::getBindFunctions)
        .filter(Objects::nonNull)
        .forEach(bindFunctionNamedRegistry::register);
    }
  }
}

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
package org.apache.ibatis.scripting.decorator.bind.impl;


import org.apache.ibatis.scripting.decorator.bind.BindFunction;
import org.apache.ibatis.scripting.decorator.bind.BindFunctionFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author andyslin
 */
public class BaseBindFunctionFactory implements BindFunctionFactory {

  @Override
  public Collection<BindFunction> getBindFunctions() {
    List<BindFunction> functions = new ArrayList<>();
    functions.add(new IfBindFunction());
    return functions;
  }
}

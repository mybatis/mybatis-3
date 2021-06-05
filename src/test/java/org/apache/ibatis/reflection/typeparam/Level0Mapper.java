/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.reflection.typeparam;

import java.util.List;
import java.util.Map;

public interface Level0Mapper<L, M, N> {

  void simpleSelectVoid(Integer param);

  double simpleSelectPrimitive(int param);

  Double simpleSelect();

  List<Double> simpleSelectList();

  Map<Integer, Double> simpleSelectMap();

  String[] simpleSelectArray();

  String[][] simpleSelectArrayOfArray();

  <K extends Calculator<?>> K simpleSelectTypeVar();

  List<? extends String> simpleSelectWildcard();

  N select(N param);

  List<N> selectList(M param1, N param2);

  List<? extends N> selectWildcardList();

  Map<N, M> selectMap();

  N[] selectArray(List<N>[] param);

  N[][] selectArrayOfArray();

  List<N>[] selectArrayOfList();

  Calculator<N> selectCalculator(Calculator<N> param);

  List<Calculator<L>> selectCalculatorList();

  interface Level0InnerMapper extends Level0Mapper<String, Long, Float> {
  }

}

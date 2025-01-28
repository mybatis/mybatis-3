/*
 *    Copyright 2009-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.collection_in_constructor;

import java.util.List;

public interface Mapper {

  Store getAStore(Integer id);

  List<Store> getStores();

  Store2 getAStore2(Integer id);

  Store3 getAStore3(Integer id);

  Store4 getAStore4(Integer id);

  Store5 getAStore5(Integer id);

  Store6 getAStore6(Integer id);

  Store7 getAStore7(Integer id);

  Store8 getAStore8(Integer id);

  Container getAContainer();

  List<Container1> getContainers();

  List<Store10> getStores10();
}

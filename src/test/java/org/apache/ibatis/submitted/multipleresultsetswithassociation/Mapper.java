/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.submitted.multipleresultsetswithassociation;

import java.util.List;

import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

public interface Mapper {

  List<OrderDetail> getOrderDetailsWithHeaders();

  @Select(value = "{ call GetOrderDetailsAndHeaders() }")
  @ResultMap("orderDetailResultMap")
  @Options(statementType = StatementType.CALLABLE, resultSets = "orderDetailResultSet,orderHeaderResultSet")
  List<OrderDetail> getOrderDetailsWithHeadersAnnotationBased();

}

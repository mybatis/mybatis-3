/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.builder.annotation;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data wrapper for {@link org.apache.ibatis.annotations.Results} annotation
 * @author Alexander Pozdnyakov
 */
class ResultsData {

  private String id;
  private List<ResultData> mappings;

  ResultsData(Results results) {
    this.id = results.id();
    this.mappings = Arrays.stream(results.value())
                          .map(result -> new ResultData(result.property(), result))
                          .collect(Collectors.toList());
  }

  ResultsData(String id, Result[] results) {
    this.id = id;
    this.mappings = Arrays.stream(results)
                          .map(result -> new ResultData(result.property(), result))
                          .collect(Collectors.toList());
  }

  ResultsData() {
    this.id = "";
    this.mappings = new ArrayList<>();
  }

  void addResult(String property, Result result) {
    this.mappings.add(new ResultData(property, result));
  }

  List<ResultData> getMappings() {
    return mappings;
  }

  String getId() {
    return id;
  }

  static class ResultData {
    private String property;
    private Result result;

    private ResultData(String property, Result result) {
      this.property = property;
      this.result = result;
    }

    String getProperty() {
      return property;
    }

    Result getResult() {
      return result;
    }
  }
}

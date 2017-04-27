/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.submitted.associationtest;

public class Car {
  private int id;
  private String type;
  private Engine engine;
  private Brakes brakes;
  
  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public Engine getEngine() {
    return engine;
  }
  public void setEngine(Engine engine) {
    this.engine = engine;
  }
  public Brakes getBrakes() {
    return brakes;
  }
  public void setBrakes(Brakes brakes) {
    this.brakes = brakes;
  }

}

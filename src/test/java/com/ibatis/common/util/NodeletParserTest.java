/*
 *    Copyright 2009-2012 the original author or authors.
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
package com.ibatis.common.util;

import com.domain.misc.Employee;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.XNode;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.Reader;
import java.util.*;

public class NodeletParserTest {

  @Test
  public void shouldParseAttribute() throws Exception {
    NodeEventParser parser = new NodeEventParser();
    NodeHandler handler = new NodeHandler();
    parser.addNodeletHandler(handler);
    parser.setVariables(new Properties() {
      {
        setProperty("id_var", "1234567890");
      }
    });
    Reader resource = Resources.getResourceAsReader("com/resources/nodelet_test.xml");
    parser.parse(resource);
    Employee emp = handler.getEmployee();
    assertEquals(1234567890, emp.getId());
    assertEquals("Jim", emp.getFirstName());
    assertEquals("Smith", emp.getLastName());
    assertEquals(new Date(1970 - 1900, 6 - 1, 15), emp.getBirthDate());
    assertEquals(5.8, emp.getHeight(), 0.0001);
    assertEquals("ft", emp.getHeightUnits());
    assertEquals(200, emp.getWeight(), 0.0001);
    assertEquals("lbs", emp.getWeightUnits());
  }


  public static class NodeHandler {

    private Employee employee = new Employee();
    private int year;
    private int month;
    private int day;

    public Employee getEmployee() {
      return employee;
    }

    @NodeEvent("/employee")
    public void id(XNode node) {
      employee.setId(node.getIntAttribute("id", 0));
    }

    @NodeEvent("/employee/first_name")
    public void firstName(XNode node) {
      employee.setFirstName(node.getStringBody(""));
    }

    @NodeEvent("/employee/last_name")
    public void lastName(XNode node) {
      employee.setLastName(node.getStringBody(""));
    }

    @NodeEvent("/employee/birth_date/year")
    public void year(XNode node) {
      year = node.getIntBody(0);
    }

    @NodeEvent("/employee/birth_date/month")
    public void month(XNode node) {
      month = node.getIntBody(0);
    }

    @NodeEvent("/employee/birth_date/day")
    public void day(XNode node) {
      day = node.getIntBody(0);
    }

    @NodeEvent("/employee/birth_date/end()")
    public void birth_date(XNode node) {
      employee.setBirthDate(new Date(year - 1900, month - 1, day));
    }

    @NodeEvent("/employee/height")
    public void height(XNode node) {
      employee.setHeight(node.getDoubleBody(0.0));
      employee.setHeightUnits(node.getStringAttribute("units", ""));
    }

    @NodeEvent("/employee/weight")
    public void weight(XNode node) {
      employee.setWeight(node.getDoubleBody(0.0));
      employee.setWeightUnits(node.getStringAttribute("units", ""));
    }

  }

}

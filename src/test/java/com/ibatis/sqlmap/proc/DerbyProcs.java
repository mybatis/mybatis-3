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
package com.ibatis.sqlmap.proc;

import java.sql.*;

public class DerbyProcs {
  public static void selectRows(int p1, int p2, int p3, int p4, ResultSet[] rs1, ResultSet[] rs2) throws SQLException {
    Connection conn = DriverManager.getConnection("jdbc:default:connection");
    PreparedStatement ps1 = conn.prepareStatement("select acc_id as id from account where acc_id in (?,?)");
    ps1.setInt(1, p1);
    ps1.setInt(2, p2);
    rs1[0] = ps1.executeQuery();
    PreparedStatement ps2 = conn.prepareStatement("select acc_id as id from account where acc_id in (?,?)");
    ps2.setInt(1, p3);
    ps2.setInt(2, p4);
    rs2[0] = ps2.executeQuery();
    conn.close();
  }

}
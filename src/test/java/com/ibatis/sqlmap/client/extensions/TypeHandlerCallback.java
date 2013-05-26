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
package com.ibatis.sqlmap.client.extensions;

import java.sql.SQLException;

/*
 * A simple interface for implementing custom type handlers.
 * <p/>
 * Using this interface, you can implement a type handler that
 * will perform customized processing before parameters are set
 * on a PreparedStatement and after values are retrieved from
 * a ResultSet.  Using a custom type handler you can extend
 * the framework to handle types that are not supported, or
 * handle supported types in a different way.  For example,
 * you might use a custom type handler to implement proprietary
 * BLOB support (e.g. Oracle), or you might use it to handle
 * booleans using "Y" and "N" instead of the more typical 0/1.
 * <p/>
 * <b>EXAMPLE</b>
 * <p>Here's a simple example of a boolean handler that uses "Yes" and "No".</p>
 * <pre>
 * public class YesNoBoolTypeHandlerCallback implements TypeHandlerCallback {
 * <p/>
 *   private static final String YES = "Yes";
 *   private static final String NO = "No";
 * <p/>
 *   public Object getResult(ResultGetter getter) throws SQLException {
 *     String s = getter.getString();
 *     if (YES.equalsIgnoreCase(s)) {
 *       return new Boolean (true);
 *     } else if (NO.equalsIgnoreCase(s)) {
 *       return new Boolean (false);
 *     } else {
 *       throw new SQLException ("Unexpected value " + s + " found where "+YES+" or "+NO+" was expected.");
 *     }
 *   }
 * <p/>
 *   public void setParameter(ParameterSetter setter, Object parameter) throws SQLException {
 *     boolean b = ((Boolean)parameter).booleanValue();
 *     if (b) {
 *       setter.setString(YES);
 *     } else {
 *       setter.setString(NO);
 *     }
 *   }
 * <p/>
 *   public Object valueOf(String s) {
 *     if (YES.equalsIgnoreCase(s)) {
 *       return new Boolean (true);
 *     } else if (NO.equalsIgnoreCase(s)) {
 *       return new Boolean (false);
 *     } else {
 *       throw new SQLException ("Unexpected value " + s + " found where "+YES+" or "+NO+" was expected.");
 *     }
 *   }
 * <p/>
 * }
 * </pre>
 */
public interface TypeHandlerCallback {

  /*
   * Performs processing on a value before it is used to set
   * the parameter of a PreparedStatement.
   *
   * @param setter    The interface for setting the value on the PreparedStatement.
   * @param parameter The value to be set.
   * @throws java.sql.SQLException If any error occurs.
   */
  public void setParameter(ParameterSetter setter, Object parameter)
      throws SQLException;

  /*
   * Performs processing on a value before after it has been retrieved
   * from a ResultSet.
   *
   * @param getter The interface for getting the value from the ResultSet.
   * @return The processed value.
   * @throws java.sql.SQLException If any error occurs.
   */
  public Object getResult(ResultGetter getter)
      throws SQLException;

  /*
   * Casts the string representation of a value into a type recognized by
   * this type handler.  This method is used to translate nullValue values
   * into types that can be appropriately compared.  If your custom type handler
   * cannot support nullValues, or if there is no reasonable string representation
   * for this type (e.g. File type), you can simply return the String representation
   * as it was passed in.  It is not recommended to return null, unless null was passed
   * in.
   *
   * @param s A string representation of a valid value for this type.
   * @return One of the following:
   *         <ol>
   *         <li>the casted repersentation of the String value,</li>
   *         <li>the string as is,</li>
   *         <li>null, only if null was passed in.</li>
   *         </ol>
   */
  public Object valueOf(String s);

}

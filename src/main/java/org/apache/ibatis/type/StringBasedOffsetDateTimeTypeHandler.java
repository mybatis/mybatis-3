/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.ibatis.lang.UsesJava8;

/**
 * This is an alternative implementation to the
 * {@link OffsetDateTimeTypeHandler}. In contrast to the former, it is capable
 * of actually retaining information about the time zone offset and does not
 * assume it to be the same as the system default.
 * <p>
 * In order for this to work however, it does not work on TIMESTAMPs but rather
 * on Strings that represent the point in time in the ISO format.
 * <p>
 * For instance, when working with an Oracle database and having a column that
 * is of type "TIMESTAMP WITH TIME ZONE" instead of writing:
 * <p>
 * <code>
 * SELECT MY_COLUMN FROM MY_TABLE
 * </code>
 * <p>
 * one would have to write the following:
 * <p>
 * <code> SELECT to_char(MY_COLUMN,'YYYY-MM-DD"T"HH24:MI:SSFFTZH:TZM') AS
 * MY_COLUMN FROM MY_TABLE
 * </code>
 * <p>
 * Then, this type handler can be used to correctly convert the timestamp stored
 * in the cell into the equivalent OffsetDateTime Java object.
 *
 * @author Malte Mauelshagen
 *
 */
@UsesJava8
public class StringBasedOffsetDateTimeTypeHandler extends BaseTypeHandler<OffsetDateTime> {

  /** Example: 2011-12-03T10:15:30+01:00 */
  public static final DateTimeFormatter OFFSET_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  @Override
  public void setNonNullParameter(PreparedStatement aPreparedStatement, int anIndex, OffsetDateTime aParameter, JdbcType aJdbcType) throws SQLException {
    String formattedOffsetDateTime = aParameter.format(OFFSET_DATE_TIME_FORMATTER);
    aPreparedStatement.setString(anIndex, formattedOffsetDateTime);
  }

  @Override
  public OffsetDateTime getNullableResult(ResultSet aResultSet, String aColumnName) throws SQLException {
    String tmpDateString = aResultSet.getString(aColumnName);
    OffsetDateTime tmpOffsetDateTime = parseOffsetDateTimeString(tmpDateString);
    return tmpOffsetDateTime;
  }

  @Override
  public OffsetDateTime getNullableResult(ResultSet aResultSet, int aColumnName) throws SQLException {
    String tmpDateString = aResultSet.getString(aColumnName);
    OffsetDateTime tmpOffsetDateTime = parseOffsetDateTimeString(tmpDateString);
    return tmpOffsetDateTime;
  }

  @Override
  public OffsetDateTime getNullableResult(CallableStatement aCallableStatement, int aColumnIndex) throws SQLException {
    String tmpDateString = aCallableStatement.getString(aColumnIndex);
    OffsetDateTime tmpOffsetDateTime = parseOffsetDateTimeString(tmpDateString);
    return tmpOffsetDateTime;
  }

  private OffsetDateTime parseOffsetDateTimeString(String aDateString) {
    if (aDateString == null) {
      return null;
    }
    try {
      OffsetDateTime tmpOffsetDateTime = OffsetDateTime.parse(aDateString, OFFSET_DATE_TIME_FORMATTER);
      return tmpOffsetDateTime;
    } catch (DateTimeParseException e) {
      final String tmpMessage = "Note, that the Date-String is expected to come in the following format: 2011-12-03T10:15:30+01:00" + " Pay attention to the spaces! Make sure to adapt your to_char() method accordingly.";
      throw new IllegalArgumentException(tmpMessage, e);
    }
  }

}

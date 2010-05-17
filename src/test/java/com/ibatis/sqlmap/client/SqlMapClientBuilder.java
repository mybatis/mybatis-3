package com.ibatis.sqlmap.client;

import com.ibatis.sqlmap.engine.builder.Ibatis2Configuration;
import com.ibatis.sqlmap.engine.builder.XmlSqlMapConfigParser;
import com.ibatis.sqlmap.engine.impl.SqlMapClientImpl;

import java.io.Reader;
import java.util.Properties;

/**
 * Builds SqlMapClient instances from a supplied resource (e.g. XML configuration file)
 * <p/>
 * The SqlMapClientBuilder class is responsible for parsing configuration documents
 * and building the SqlMapClient instance.  Its current implementation works with
 * XML configuration files (e.g. sql-map-config.xml).
 * <p/>
 * Example:
 * <pre>
 * Reader reader = Resources.getResourceAsReader("properties/sql-map-config.xml");
 * SqlMapClient client = SqlMapClientBuilder.buildSqlMapClient (reader);
 * </pre>
 * <p/>
 * Examples of the XML document structure used by SqlMapClientBuilder can
 * be found at the links below.
 * <p/>
 * Note: They might look big, but they're mostly comments!
 * <ul>
 * <li> <a href="sql-map-config.txt">The SQL Map Config File</a>
 * <li> <a href="sql-map.txt">An SQL Map File</a>
 * <ul>
 */
public class SqlMapClientBuilder {

  /**
   * No instantiation allowed.
   */
  protected SqlMapClientBuilder() {
  }

  /**
   * Builds an SqlMapClient using the specified reader.
   *
   * @param reader A Reader instance that reads an sql-map-config.xml file.
   *               The reader should read an well formed sql-map-config.xml file.
   * @return An SqlMapClient instance.
   */
  public static SqlMapClient buildSqlMapClient(Reader reader) {
    XmlSqlMapConfigParser configParser = new XmlSqlMapConfigParser(reader);
    configParser.parse();
    return new SqlMapClientImpl((Ibatis2Configuration) configParser.getConfiguration());
  }

  /**
   * Builds an SqlMapClient using the specified reader and properties file.
   * <p/>
   *
   * @param reader A Reader instance that reads an sql-map-config.xml file.
   *               The reader should read an well formed sql-map-config.xml file.
   * @param props  Properties to be used to provide values to dynamic property tokens
   *               in the sql-map-config.xml configuration file.  This provides an easy way to
   *               achieve some level of programmatic configuration.
   * @return An SqlMapClient instance.
   */
  public static SqlMapClient buildSqlMapClient(Reader reader, Properties props) {
    XmlSqlMapConfigParser configParser = new XmlSqlMapConfigParser(reader, props);
    configParser.parse();
    return new SqlMapClientImpl((Ibatis2Configuration) configParser.getConfiguration());
  }

}

package com.ibatis.dao.client;

import com.ibatis.dao.engine.builder.xml.XmlDaoManagerBuilder;

import java.io.Reader;
import java.util.Properties;

/**
 * Builds a DaoManager given a Reader (dao.xml) and optionally a set of
 * properties.  The following is an example of a dao.xml file.
 * <ul>
 * <li> <a href="dao.txt">The DAO Configuration file (dao.xml)</a>
 * <ul>
 * <p/>
 */
public class DaoManagerBuilder {

  /**
   * No instantiation allowed.
   */
  private DaoManagerBuilder() {
  }

  /**
   * Builds a DAO Manager from the reader supplied.
   *
   * @param reader A Reader instance that reads a DAO configuration file (dao.xml).
   * @param props  A Properties instance that will be used for parameter substitution
   *               in the DAO configuration file (dao.xml).
   * @return A configured DaoManager instance
   */
  public static DaoManager buildDaoManager(Reader reader, Properties props)
      throws DaoException {
    return new XmlDaoManagerBuilder().buildDaoManager(reader, props);
  }

  /**
   * Builds a DAO Manager from the reader supplied.
   *
   * @param reader A Reader instance that reads a DAO configuration file (dao.xml).
   * @return A configured DaoManager instance
   */
  public static DaoManager buildDaoManager(Reader reader) {
    return new XmlDaoManagerBuilder().buildDaoManager(reader);
  }


}

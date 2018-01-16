/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.parsing.sql;

import java.io.BufferedReader;
import java.io.Reader;
import java.sql.Connection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public abstract class SqlParser implements Iterable<String> {

  protected BufferedReader lineReader;

  private boolean iteratorRetrieved = false;

  public abstract Integer suitabilityFor(Connection c, Map<Object, Object> additionalProps);

  public void setSqlReader(Reader reader) {
    if (this.lineReader != null) {
      throw new IllegalStateException("Reader already set");
    }
    this.lineReader = new BufferedReader(reader);
  }

  protected abstract String readNextStatement();

  @Override
  public Iterator<String> iterator() {
    if (lineReader == null) {
      throw new IllegalStateException("Reader not set");
    }
    if (iteratorRetrieved) {
      throw new IllegalStateException("iterator() may only be called once");
    }
    iteratorRetrieved = true;
    return new StatementIterator();
  }

  public abstract SqlParser withProperties(Map<Object, Object> additionalProps);

  class StatementIterator implements Iterator<String> {

    private String nextStatement;

    StatementIterator() {
      nextStatement = readNextStatement();
    }

    @Override
    public boolean hasNext() {
      return nextStatement != null;
    }

    @Override
    public String next() {
      if (nextStatement == null) {
        throw new NoSuchElementException();
      }
      String retVal = nextStatement;
      nextStatement = readNextStatement();
      return retVal;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}

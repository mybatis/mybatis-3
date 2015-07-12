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
package org.apache.ibatis.cursor.defaults;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetWrapper;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Guillaume Darmont / guillaume@dropinocean.com
 * @author Kazuki Shimizu
 */
public class DefaultCursor<T> implements Cursor<T> {

    // ResultSetHandler stuff
    private final DefaultResultSetHandler resultSetHandler;
    private final ResultMap resultMap;
    private final ResultSetWrapper rsw;
    private final RowBounds rowBounds;
    private final ObjectWrapperResultHandler<T> objectWrapperResultHandler = new ObjectWrapperResultHandler<T>();

    private int currentIndex = -1;

    private boolean iteratorAlreadyOpened = false;

    private boolean opened = false;

    private boolean resultSetConsumed = false;

    public DefaultCursor(DefaultResultSetHandler resultSetHandler, ResultMap resultMap, ResultSetWrapper rsw, RowBounds rowBounds) {
        this.resultSetHandler = resultSetHandler;
        this.resultMap = resultMap;
        this.rsw = rsw;
        this.rowBounds = rowBounds;
    }

    @Override
    public boolean isOpen() {
        return opened;
    }

    @Override
    public boolean isConsumed() {
        return resultSetConsumed;
    }

    @Override
    public int getCurrentIndex() {
        return currentIndex;
    }

    @Override
    public Iterator<T> iterator() {
        return new CursorIterator();
    }

    @Override
    public void close() {
        ResultSet rs = rsw.getResultSet();
        try {
            if (rs != null) {
                Statement statement = rs.getStatement();

                rs.close();
                if (statement != null) {
                    statement.close();
                }
            }
            opened = false;
        } catch (SQLException e) {
            // ignore
        }
    }

    protected T fetchNextUsingRowBound() {
        T result = fetchNextObjectFromDatabase();
        while (currentIndex < rowBounds.getOffset()) {
            result = fetchNextObjectFromDatabase();
        }
        return result;
    }

    protected T fetchNextObjectFromDatabase() {
        if (resultSetConsumed) {
            return null;
        }

        try {
            opened = true;
            resultSetHandler.handleRowValues(rsw, resultMap, objectWrapperResultHandler, RowBounds.DEFAULT, null);
        } catch (SQLException e) {
            ExceptionFactory.wrapException("Error fetching next object from database at the DefaultCursor.", e);
        }

        T next = objectWrapperResultHandler.result;
        if (next != null) {
            currentIndex++;
        }
        // No more object or limit reached
        if (next == null || (getReadItemsCount() == rowBounds.getOffset() + rowBounds.getLimit())) {
            close();
            resultSetConsumed = true;
        }
        objectWrapperResultHandler.result = null;

        return next;
    }

    private int getReadItemsCount() {
        return currentIndex + 1;
    }

    private static class ObjectWrapperResultHandler<E> implements ResultHandler {

        private E result;

        @Override
        public void handleResult(ResultContext context) {
            this.result = (E) context.getResultObject();
            context.stop();
        }
    }

    private class CursorIterator implements Iterator<T> {

        /**
         * Holder for the next objet to be returned
         */
        T object;

        public CursorIterator() {
            if (iteratorAlreadyOpened) {
                throw new IllegalStateException("Cannot open more than one iterator on a Cursor");
            }
            iteratorAlreadyOpened = true;
        }

        @Override
        public boolean hasNext() {
            if (object == null) {
                try {
                    object = fetchNextUsingRowBound();
                } finally {
                    ErrorContext.instance().reset();
                }
            }
            return object != null;
        }

        @Override
        public T next() {
            // Fill next with object fetched from hasNext()
            T next = object;

            if (next == null) {
                try {
                    next = fetchNextUsingRowBound();
                } finally {
                    ErrorContext.instance().reset();
                }
            }

            if (next != null) {
                object = null;
                return next;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Cannot currently remove element from Cursor");
        }
    }
}

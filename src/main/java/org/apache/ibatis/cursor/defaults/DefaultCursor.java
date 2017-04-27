/**
 *    Copyright 2009-2016 the original author or authors.
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
 * This is the default implementation of a MyBatis Cursor.
 * This implementation is not thread safe.
 *
 * @author Guillaume Darmont / guillaume@dropinocean.com
 */
public class DefaultCursor<T> implements Cursor<T> {

    // ResultSetHandler stuff
    private final DefaultResultSetHandler resultSetHandler;
    private final ResultMap resultMap;
    private final ResultSetWrapper rsw;
    private final RowBounds rowBounds;
    private final ObjectWrapperResultHandler<T> objectWrapperResultHandler = new ObjectWrapperResultHandler<T>();

    private final CursorIterator cursorIterator = new CursorIterator();
    private boolean iteratorRetrieved;

    private CursorStatus status = CursorStatus.CREATED;
    private int indexWithRowBound = -1;

    private enum CursorStatus {

        /**
         * A freshly created cursor, database ResultSet consuming has not started
         */
        CREATED,
        /**
         * A cursor currently in use, database ResultSet consuming has started
         */
        OPEN,
        /**
         * A closed cursor, not fully consumed
         */
        CLOSED,
        /**
         * A fully consumed cursor, a consumed cursor is always closed
         */
        CONSUMED
    }

    public DefaultCursor(DefaultResultSetHandler resultSetHandler, ResultMap resultMap, ResultSetWrapper rsw, RowBounds rowBounds) {
        this.resultSetHandler = resultSetHandler;
        this.resultMap = resultMap;
        this.rsw = rsw;
        this.rowBounds = rowBounds;
    }

    @Override
    public boolean isOpen() {
        return status == CursorStatus.OPEN;
    }

    @Override
    public boolean isConsumed() {
        return status == CursorStatus.CONSUMED;
    }

    @Override
    public int getCurrentIndex() {
        return rowBounds.getOffset() + cursorIterator.iteratorIndex;
    }

    @Override
    public Iterator<T> iterator() {
        if (iteratorRetrieved) {
            throw new IllegalStateException("Cannot open more than one iterator on a Cursor");
        }
        iteratorRetrieved = true;
        return cursorIterator;
    }

    @Override
    public void close() {
        if (isClosed()) {
            return;
        }

        ResultSet rs = rsw.getResultSet();
        try {
            if (rs != null) {
                Statement statement = rs.getStatement();

                rs.close();
                if (statement != null) {
                    statement.close();
                }
            }
            status = CursorStatus.CLOSED;
        } catch (SQLException e) {
            // ignore
        }
    }

    protected T fetchNextUsingRowBound() {
        T result = fetchNextObjectFromDatabase();
        while (result != null && indexWithRowBound < rowBounds.getOffset()) {
            result = fetchNextObjectFromDatabase();
        }
        return result;
    }

    protected T fetchNextObjectFromDatabase() {
        if (isClosed()) {
            return null;
        }

        try {
            status = CursorStatus.OPEN;
            resultSetHandler.handleRowValues(rsw, resultMap, objectWrapperResultHandler, RowBounds.DEFAULT, null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        T next = objectWrapperResultHandler.result;
        if (next != null) {
            indexWithRowBound++;
        }
        // No more object or limit reached
        if (next == null || (getReadItemsCount() == rowBounds.getOffset() + rowBounds.getLimit())) {
            close();
            status = CursorStatus.CONSUMED;
        }
        objectWrapperResultHandler.result = null;

        return next;
    }

    private boolean isClosed() {
        return status == CursorStatus.CLOSED || status == CursorStatus.CONSUMED;
    }

    private int getReadItemsCount() {
        return indexWithRowBound + 1;
    }

    private static class ObjectWrapperResultHandler<T> implements ResultHandler<T> {

        private T result;

        @Override
        public void handleResult(ResultContext<? extends T> context) {
            this.result = context.getResultObject();
            context.stop();
        }
    }

    private class CursorIterator implements Iterator<T> {

        /**
         * Holder for the next object to be returned
         */
        T object;

        /**
         * Index of objects returned using next(), and as such, visible to users.
         */
        int iteratorIndex = -1;

        @Override
        public boolean hasNext() {
            if (object == null) {
                object = fetchNextUsingRowBound();
            }
            return object != null;
        }

        @Override
        public T next() {
            // Fill next with object fetched from hasNext()
            T next = object;

            if (next == null) {
                next = fetchNextUsingRowBound();
            }

            if (next != null) {
                object = null;
                iteratorIndex++;
                return next;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Cannot remove element from Cursor");
        }
    }
}

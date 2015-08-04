package org.apache.ibatis.cursor;

import org.apache.ibatis.exceptions.PersistenceException;

/**
 * @author Guillaume Darmont / guillaume@dropinocean.com
 */
public class CursorException extends PersistenceException {

    public CursorException(String message) {
        super(message);
    }

    public CursorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CursorException(Throwable cause) {
        super(cause);
    }
}

package org.apache.ibatis.exceptions;

import org.apache.ibatis.builder.BuilderException;

public class UnCorrectNodeTypeException extends BuilderException {
    public UnCorrectNodeTypeException() {
        super();
    }

    public UnCorrectNodeTypeException(String message) {
        super(message);
    }

    public UnCorrectNodeTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnCorrectNodeTypeException(Throwable cause) {
        super(cause);
    }
}

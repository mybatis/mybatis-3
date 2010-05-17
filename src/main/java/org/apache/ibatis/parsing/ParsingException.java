package org.apache.ibatis.parsing;

import org.apache.ibatis.exceptions.IbatisException;

public class ParsingException extends IbatisException {
  public ParsingException() {
    super();
  }

  public ParsingException(String message) {
    super(message);
  }

  public ParsingException(String message, Throwable cause) {
    super(message, cause);
  }

  public ParsingException(Throwable cause) {
    super(cause);
  }
}

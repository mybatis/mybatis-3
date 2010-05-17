package org.apache.ibatis.plugin;

import org.apache.ibatis.exceptions.IbatisException;

public class PluginException extends IbatisException {

  public PluginException() {
    super();
  }

  public PluginException(String message) {
    super(message);
  }

  public PluginException(String message, Throwable cause) {
    super(message, cause);
  }

  public PluginException(Throwable cause) {
    super(cause);
  }
}

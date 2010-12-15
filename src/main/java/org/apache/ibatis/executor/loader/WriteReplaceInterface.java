package org.apache.ibatis.executor.loader;

import java.io.ObjectStreamException;

public interface WriteReplaceInterface {

  Object writeReplace() throws ObjectStreamException;

}

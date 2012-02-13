package org.apache.ibatis.submitted.result_handler_type;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.reflection.factory.DefaultObjectFactory;

public class ObjectFactory extends DefaultObjectFactory {

  private static final long serialVersionUID = -8855120656740914948L;

  @Override
  protected Class<?> resolveInterface(Class<?> type) {
    Class<?> classToCreate;
    if (type == Map.class) {
      classToCreate = LinkedHashMap.class;
    } else if (type == List.class || type == Collection.class) {
      classToCreate = LinkedList.class;
    } else {
      classToCreate = super.resolveInterface(type);
    }
    return classToCreate;
  }
}

package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MapWrapper extends BaseWrapper {

  private Map map;
  private Object[] keyArray;


  public MapWrapper(MetaObject metaObject, Map map) {
    super(metaObject);
    this.map = map;
    updateKeyArray();
  }

  public Object get(PropertyTokenizer prop) {
    if (prop.getIndex() != null) {
      Object collection = resolveCollection(prop, map);
      return getCollectionValue(prop, collection);
    } else {
      return map.get(prop.getName());
    }
  }

  public void set(PropertyTokenizer prop, Object value) {
    if (prop.getIndex() != null) {
      Object collection = resolveCollection(prop, map);
      setCollectionValue(prop, collection, value);
    } else {
      map.put(prop.getName(), value);
    }
    updateKeyArray();
  }

  @SuppressWarnings("unchecked")
  public String findProperty(String name) {
    updateKeyArray();
    if (name != null) {
      Arrays.binarySearch(keyArray, name, new Comparator() {
        public int compare(Object o1, Object o2) {
          if (o1 == o2) {
            return 0;
          } else if (o1 == null && o2 == null) {
            return 0;
          } else if (o1 == null) {
            return -1;
          } else if (o2 == null) {
            return 1;
          }
          return ((String) o1).toLowerCase().compareTo(((String) o2).toLowerCase());
        }
      });
    }
    return name;
  }

  public String[] getGetterNames() {
    return (String[]) map.keySet().toArray(new String[map.keySet().size()]);
  }

  public String[] getSetterNames() {
    return (String[]) map.keySet().toArray(new String[map.keySet().size()]);
  }

  public Class getSetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
      if (metaValue == MetaObject.NULL_META_OBJECT) {
        return Object.class;
      } else {
        return metaValue.getSetterType(prop.getChildren());
      }
    } else {
      if (map.get(name) != null) {
        return map.get(name).getClass();
      } else {
        return Object.class;
      }
    }
  }

  public Class getGetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
      if (metaValue == MetaObject.NULL_META_OBJECT) {
        return Object.class;
      } else {
        return metaValue.getGetterType(prop.getChildren());
      }
    } else {
      if (map.get(name) != null) {
        return map.get(name).getClass();
      } else {
        return Object.class;
      }
    }
  }

  public boolean hasSetter(String name) {
    return true;
  }

  public boolean hasGetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      if (map.containsKey(prop.getIndexedName())) {
        MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
        if (metaValue == MetaObject.NULL_META_OBJECT) {
          return map.containsKey(name);
        } else {
          return metaValue.hasGetter(prop.getChildren());
        }
      } else {
        return false;
      }
    } else {
      return map.containsKey(name);
    }
  }

  public MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory) {
    HashMap map = new HashMap();
    set(prop, map);
    return MetaObject.forObject(map, metaObject.getObjectFactory(), metaObject.getObjectWrapperFactory());
  }

  private void updateKeyArray() {
    keyArray = map.keySet().toArray();
    Arrays.sort(keyArray);
  }

}

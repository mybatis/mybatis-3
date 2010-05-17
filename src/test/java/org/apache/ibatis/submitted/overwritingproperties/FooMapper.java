package org.apache.ibatis.submitted.overwritingproperties;

public interface FooMapper {

  void insertFoo(Foo foo);

  Foo selectFoo();

  int deleteAllFoo();

}

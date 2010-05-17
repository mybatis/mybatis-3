package org.apache.ibatis.submitted.null_associations;

public interface FooMapper {

  void insertFoo(Foo foo);

  Foo selectFoo();

  int deleteAllFoo();

}

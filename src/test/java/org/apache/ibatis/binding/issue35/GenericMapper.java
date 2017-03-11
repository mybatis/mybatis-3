package org.apache.ibatis.binding.issue35;

/**
 * Basic mapper interface for the common CRUD operations
 */
public interface GenericMapper<E> {

    int insert(E e);

    int update(E e);

    int deleteById(Integer id);

    E selectById(Integer id);
}

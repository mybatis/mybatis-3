package org.apache.ibatis.submitted.associationtest;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by root on 15-4-27.
 */
public interface GenericMapper<T, E> {
    @Select("select carid as id, cartype as type from cars where carid=#{id}")
    T getElement(@Param("id") int id);

    @Select("select carid, cartype from cars")
    E getOther();

    @Select("select cartype from cars")
    List<E> getAllCarTypes();
}

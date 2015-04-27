package org.apache.ibatis.submitted.associationtest;

import org.apache.ibatis.annotations.Select;

/**
 * Created by root on 15-4-27.
 */
public interface AutomoblieMapper<T> {
    @Select("select cartype from cars limit 1")
    T getMobileElement();
}

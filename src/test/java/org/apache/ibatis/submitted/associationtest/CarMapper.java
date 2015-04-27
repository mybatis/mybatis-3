package org.apache.ibatis.submitted.associationtest;

/**
 * Created by root on 15-4-27.
 */
public interface CarMapper<E> extends GenericMapper<Car,E>, AutomoblieMapper<E> {
}

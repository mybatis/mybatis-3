package com.ibatis.common.util;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NodeEvent {
  String value();
}

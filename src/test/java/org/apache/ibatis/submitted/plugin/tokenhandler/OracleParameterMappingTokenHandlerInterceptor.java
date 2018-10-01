/*
 * Copyright 2017 Focus Technology, Co., Ltd. All rights reserved.
 */
/**
 * 
 */
package org.apache.ibatis.submitted.plugin.tokenhandler;

import java.util.List;
import java.util.Properties;

import org.apache.ibatis.builder.SqlSourceBuilder.ParameterMappingTokenHandler;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.type.JdbcType;

/**
 * FocusDateTypeHandler.java
 *
 * @author chuyifan
 */
/**
 * @author chuyifan
 */
@Intercepts({@Signature(type = TokenHandler.class, method = "handleToken", args = {String.class})})
public class OracleParameterMappingTokenHandlerInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object orignResult = invocation.proceed();
        ParameterMappingTokenHandler parameterMappingHandler = (ParameterMappingTokenHandler) invocation.getTarget();
        List<ParameterMapping> parameterMapping = parameterMappingHandler.getParameterMappings();
        if (JdbcType.DATETIME.equals(parameterMapping.get(parameterMapping.size() - 1).getJdbcType())) {
            return "to_date(?,'yyyy-mm-dd hh24:mi:ss')";
        }
        return orignResult;
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof ParameterMappingTokenHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {

    }
}

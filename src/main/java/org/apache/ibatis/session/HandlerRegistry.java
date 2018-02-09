/**
 * Copyright 2009-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.ibatis.session;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.binding.handler.MapperHandler;
import org.apache.ibatis.binding.handler.ParamResolverFactory;
import org.apache.ibatis.binding.handler.impl.DefaultParamResolverFactory;
import org.apache.ibatis.binding.handler.impl.FlushMapperHandler;
import org.apache.ibatis.binding.handler.impl.SelectMapperHandler;
import org.apache.ibatis.binding.handler.impl.UpdateMapperHandler;
import org.apache.ibatis.scripting.xmltags.sqlconfigfunction.SqlConfigFunction;
import org.apache.ibatis.scripting.xmltags.sqlconfigfunction.SqlConfigFunctionFactory;
import org.apache.ibatis.scripting.xmltags.sqlconfigfunction.impl.BaseSqlConfigFunctionFactory;

/**
 * @since 3.4.6
 */
public class HandlerRegistry {

    protected boolean enableMapperHandler = true;
    protected ParamResolverFactory paramResolverFactory = new DefaultParamResolverFactory();
    protected final List<MapperHandler> mapperHandlers = new LinkedList<MapperHandler>();
    protected final Map<String, SqlConfigFunction> sqlConfigFunctions = new ConcurrentHashMap<String, SqlConfigFunction>();

    public HandlerRegistry() {
        this.init();
    }

    protected void init() {
        mapperHandlers.add(new UpdateMapperHandler());
        mapperHandlers.add(new SelectMapperHandler());
        mapperHandlers.add(new FlushMapperHandler());
        registerSqlConfigFunctionFactory(new BaseSqlConfigFunctionFactory());
    }

    public boolean isEnableMapperHandler() {
        return enableMapperHandler;
    }

    public void setEnableMapperHandler(boolean enableMapperHandler) {
        this.enableMapperHandler = enableMapperHandler;
    }

    public ParamResolverFactory getParamResolverFactory() {
        return paramResolverFactory;
    }

    public void setParamResolverFactory(ParamResolverFactory paramResolverFactory) {
        if (null != paramResolverFactory) {
            this.paramResolverFactory = paramResolverFactory;
        }
    }

    public List<MapperHandler> getMapperHandlers() {
        return mapperHandlers;
    }

    public void setMapperHandlers(List<MapperHandler> mapperHandlers) {
        if (null != mapperHandlers && !mapperHandlers.isEmpty()) {
            this.mapperHandlers.addAll(mapperHandlers);
            Collections.sort(this.mapperHandlers, new Comparator<MapperHandler>() {
                @Override
                public int compare(MapperHandler o1, MapperHandler o2) {
                    int i1 = o1.getOrder();
                    int i2 = o2.getOrder();
                    return (i1 < i2) ? -1 : (i1 > i2) ? 1 : 0;
                }
            });
        }
    }

    public Map<String, SqlConfigFunction> getSqlConfigFunctions() {
        return sqlConfigFunctions;
    }

    public void registerSqlConfigFunction(SqlConfigFunction... sqlConfigFunctions) {
        for (SqlConfigFunction sqlConfigFunction : sqlConfigFunctions) {
            String name = sqlConfigFunction.getName().toUpperCase();
            SqlConfigFunction old = this.sqlConfigFunctions.get(name);
            if (null == old || sqlConfigFunction.getOrder() < old.getOrder()) {
                this.sqlConfigFunctions.put(name, sqlConfigFunction);
            }
        }
    }

    public void registerSqlConfigFunctionFactory(SqlConfigFunctionFactory... sqlConfigFunctionFactorys) {
        for (SqlConfigFunctionFactory sqlConfigFunctionFactory : sqlConfigFunctionFactorys) {
            Collection<SqlConfigFunction> sqlConfigFunctions = sqlConfigFunctionFactory.getSqlConfigFunctions();
            if (null != sqlConfigFunctions) {
                for (SqlConfigFunction sqlConfigFunction : sqlConfigFunctions) {
                    registerSqlConfigFunction(sqlConfigFunction);
                }
            }
        }
    }
}

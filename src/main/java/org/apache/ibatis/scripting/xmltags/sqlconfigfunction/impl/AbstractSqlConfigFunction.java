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
package org.apache.ibatis.scripting.xmltags.sqlconfigfunction.impl;

import org.apache.ibatis.scripting.xmltags.sqlconfigfunction.SqlConfigFunction;

/**
 * @author Felix Lin
 * @since 3.4.6
 */
public abstract class AbstractSqlConfigFunction implements SqlConfigFunction {

    @Override
    public int getOrder() {
        return 0;
    }

    protected String join(String[] arr, String separator) {
        if (null == arr || arr.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Object obj : arr) {
            if (null != obj) {
                sb.append(separator).append(obj);
            }
        }
        return sb.substring(separator.length()).toString();
    }

    protected void assertEqualArgsCount(String[] args, int count) {
        if (null != args && args.length != count) {
            throw new IllegalArgumentException("the count of sql-config-function [" + getName() + "] args must be " + count + ", but actual is " + args.length);
        }
    }

    protected void assertAtLeastArgsCount(String[] args, int count) {
        if (null != args && args.length < count) {
            throw new IllegalArgumentException("the count of sql-config-function [" + getName() + "] args at least is " + count + ", but actual is " + args.length);
        }
    }
}

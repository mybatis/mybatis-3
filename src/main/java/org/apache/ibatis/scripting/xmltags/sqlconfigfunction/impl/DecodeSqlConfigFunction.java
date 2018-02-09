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

/**
 * @author Felix Lin
 * @since 3.4.6
 */
/* package */ class DecodeSqlConfigFunction extends AbstractDatabaseIdSqlConfigFunction {

    @Override
    public String getName() {
        return "decode";
    }

    @Override
    public String eval(String databaseId, String[] args) {
        super.assertAtLeastArgsCount(args, 3);
        if (databaseId.indexOf("oracle") != -1) {
            return "DECODE(" + join(args, ",") + ")";
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("CASE ").append(args[0]);
            int i = 2, l = args.length;
            for (; i < l; i = i + 2) {
                sb.append(" WHEN ").append(args[i - 1]).append(" THEN ").append(args[i]);
            }
            if (i == l) {//结束循环时，两者相等说明最后一个参数未使用
                sb.append(" ELSE ").append(args[l - 1]);
            }
            sb.append(" END");
            return sb.toString();
        }
    }
}

/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.pageBound;

import java.util.Map;

import org.apache.ibatis.session.PageBounds;
import org.apache.ibatis.session.PageResult;



public interface UserMapper {

	PageResult<User> selectAll(Map<String,Object> param, PageBounds pageBounds );
	
	PageResult<User> unionSelect(Map<String,Object> param, PageBounds pageBounds );
  
}

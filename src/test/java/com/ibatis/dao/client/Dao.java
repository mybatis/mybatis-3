/*
 *    Copyright 2009-2012 the original author or authors.
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
package com.ibatis.dao.client;

/*
 * The interface that identifies and describes Data Access Objects.
 * <p/>
 * No methods are declared by this interface.  However, if you provide
 * a constructor with a single parameter of type DaoManager,
 * that constructor will be used to instantiate the Dao such and
 * the managing DaoManager instance will be passed in as a parameter.
 * The DaoManager instance will allow you to easily access transactions
 * and other DAOs.
 * <p/>
 */
public interface Dao {

}

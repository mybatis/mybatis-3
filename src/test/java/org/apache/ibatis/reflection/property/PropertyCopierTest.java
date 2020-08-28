/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.reflection.property;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author zhanghanlin
 */
class PropertyCopierTest {

    @Test
    void testCopyBeanProperties() {

      PropertyTestClass sourceBean =
        new PropertyTestClass("superStr", "childStr", 1, false);
      PropertyTestClass destinationBean =
        new PropertyTestClass();
      PropertyCopier.copyBeanProperties(PropertyTestClass.class,sourceBean,destinationBean);

      Assertions.assertEquals("childStr",destinationBean.getTestStr());
      Assertions.assertEquals(1,destinationBean.getTestInt());
      Assertions.assertEquals(false,destinationBean.getTestBool());
      Assertions.assertEquals("superStr",destinationBean.getSupStr());

    }


}

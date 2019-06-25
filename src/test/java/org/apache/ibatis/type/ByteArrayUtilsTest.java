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
package org.apache.ibatis.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ByteArrayUtilsTest {

  @Test
  public void testConvertToPrimitiveArray() {
    assertArrayEquals(new byte[] {},
        ByteArrayUtils.convertToPrimitiveArray(new Byte[0]));
    assertArrayEquals(new byte[] {0, 1, 2, 3},
        ByteArrayUtils.convertToPrimitiveArray(new Byte[] {0, 1, 2, 3}));
  }

  @Test
  public void testConvertToObjectArray() {
    assertArrayEquals(new Byte[] {},
        ByteArrayUtils.convertToObjectArray(new byte[0]));
    assertArrayEquals(new Byte[] {0, 1, 2, 3},
        ByteArrayUtils.convertToObjectArray(new byte[] {0, 1, 2, 3}));
  }
}

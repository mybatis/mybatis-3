//--------------------------------------------------------------------------
//	Copyright (c) 1998-2004, Drew Davidson and Luke Blanshard
//  All rights reserved.
//
//	Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//	Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//	Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//	Neither the name of the Drew Davidson nor the names of its contributors
//  may be used to endorse or promote products derived from this software
//  without specific prior written permission.
//
//	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
//  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
//  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
//  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
//  OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
//  AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
//  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
//  THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
//  DAMAGE.
//--------------------------------------------------------------------------
package org.apache.ibatis.ognl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ObjectArrayPool extends Object {
  private IntHashMap pools = new IntHashMap(23);

  public static class SizePool extends Object {
    private List arrays = new ArrayList();
    private int arraySize;
    private int size;
    private int created = 0;
    private int recovered = 0;
    private int recycled = 0;

    public SizePool(int arraySize) {
      this(arraySize, 0);
    }

    public SizePool(int arraySize, int initialSize) {
      super();
      this.arraySize = arraySize;
      for (int i = 0; i < initialSize; i++) {
        arrays.add(new Object[arraySize]);
      }
      created = size = initialSize;
    }

    public int getArraySize() {
      return arraySize;
    }

    public Object[] create() {
      Object[] result;

      if (size > 0) {
        result = (Object[]) arrays.remove(size - 1);
        size--;
        recovered++;
      } else {
        result = new Object[arraySize];
        created++;
      }
      return result;
    }

    public synchronized void recycle(Object[] value) {
      if (value != null) {
        if (value.length != arraySize) {
          throw new IllegalArgumentException("recycled array size " + value.length + " inappropriate for pool array size " + arraySize);
        }
        Arrays.fill(value, null);
        arrays.add(value);
        size++;
        recycled++;
      } else {
        throw new IllegalArgumentException("cannot recycle null object");
      }
    }

    /**
     * Returns the number of items in the pool
     */
    public int getSize() {
      return size;
    }

    /**
     * Returns the number of items this pool has created since
     * it's construction.
     */
    public int getCreatedCount() {
      return created;
    }

    /**
     * Returns the number of items this pool has recovered from
     * the pool since its construction.
     */
    public int getRecoveredCount() {
      return recovered;
    }

    /**
     * Returns the number of items this pool has recycled since
     * it's construction.
     */
    public int getRecycledCount() {
      return recycled;
    }
  }

  public ObjectArrayPool() {
    super();
  }

  public IntHashMap getSizePools() {
    return pools;
  }

  public synchronized SizePool getSizePool(int arraySize) {
    SizePool result = (SizePool) pools.get(arraySize);

    if (result == null) {
      pools.put(arraySize, result = new SizePool(arraySize));
    }
    return result;
  }

  public synchronized Object[] create(int arraySize) {
    return getSizePool(arraySize).create();
  }

  public synchronized Object[] create(Object singleton) {
    Object[] result = create(1);

    result[0] = singleton;
    return result;
  }

  public synchronized Object[] create(Object object1, Object object2) {
    Object[] result = create(2);

    result[0] = object1;
    result[1] = object2;
    return result;
  }

  public synchronized Object[] create(Object object1, Object object2, Object object3) {
    Object[] result = create(3);

    result[0] = object1;
    result[1] = object2;
    result[2] = object3;
    return result;
  }

  public synchronized Object[] create(Object object1, Object object2, Object object3, Object object4) {
    Object[] result = create(4);

    result[0] = object1;
    result[1] = object2;
    result[2] = object3;
    result[3] = object4;
    return result;
  }

  public synchronized Object[] create(Object object1, Object object2, Object object3, Object object4, Object object5) {
    Object[] result = create(5);

    result[0] = object1;
    result[1] = object2;
    result[2] = object3;
    result[3] = object4;
    result[4] = object5;
    return result;
  }

  public synchronized void recycle(Object[] value) {
    if (value != null) {
      getSizePool(value.length).recycle(value);
    }
  }
}

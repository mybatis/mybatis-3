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
import java.util.List;

public final class EvaluationPool extends Object {
  private List evaluations = new ArrayList();
  private int size = 0;
  private int created = 0;
  private int recovered = 0;
  private int recycled = 0;

  public EvaluationPool() {
    this(0);
  }

  public EvaluationPool(int initialSize) {
    super();
    for (int i = 0; i < initialSize; i++) {
      evaluations.add(new Evaluation(null, null));
    }
    created = size = initialSize;
  }

  /**
   * Returns an Evaluation that contains the node, source and whether it
   * is a set operation.  If there are no Evaluation objects in the
   * pool one is created and returned.
   */
  public Evaluation create(SimpleNode node, Object source) {
    return create(node, source, false);
  }

  /**
   * Returns an Evaluation that contains the node, source and whether it
   * is a set operation.  If there are no Evaluation objects in the
   * pool one is created and returned.
   */
  public synchronized Evaluation create(SimpleNode node, Object source, boolean setOperation) {
    Evaluation result;

    if (size > 0) {
      result = (Evaluation) evaluations.remove(size - 1);
      result.init(node, source, setOperation);
      size--;
      recovered++;
    } else {
      result = new Evaluation(node, source, setOperation);
      created++;
    }
    return result;
  }

  /**
   * Recycles an Evaluation
   */
  public synchronized void recycle(Evaluation value) {
    if (value != null) {
      value.reset();
      evaluations.add(value);
      size++;
      recycled++;
    }
  }

  /**
   * Recycles an of Evaluation and all of it's siblings
   * and children.
   */
  public void recycleAll(Evaluation value) {
    if (value != null) {
      recycleAll(value.getNext());
      recycleAll(value.getFirstChild());
      recycle(value);
    }
  }

  /**
   * Recycles a List of Evaluation objects
   */
  public void recycleAll(List value) {
    if (value != null) {
      for (int i = 0, icount = value.size(); i < icount; i++) {
        recycle((Evaluation) value.get(i));
      }
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

/*
 *    Copyright 2009-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.jdbc;

import java.io.IOException;

public enum LimitingRowsStrategy {
  NOP {
    @Override
    public void appendClause(SafeAppendable builder, String offset, String limit) {
      // NOP
    }
  },
  ISO {
    @Override
    public void appendClause(SafeAppendable builder, String offset, String limit) {
      if (offset != null) {
        builder.append(" OFFSET ").append(offset).append(" ROWS");
      }
      if (limit != null) {
        builder.append(" FETCH FIRST ").append(limit).append(" ROWS ONLY");
      }
    }
  },
  OFFSET_LIMIT {
    @Override
    public void appendClause(SafeAppendable builder, String offset, String limit) {
      if (limit != null) {
        builder.append(" LIMIT ").append(limit);
      }
      if (offset != null) {
        builder.append(" OFFSET ").append(offset);
      }
    }
  };

  public abstract void appendClause(SafeAppendable builder, String offset, String limit);

  public void applyLimitToSelect(SafeAppendable builder, String offset, String limit) {
    this.appendClause(builder, offset, limit);
  }

}

class SafeAppendable {
  private final Appendable appendable;
  private boolean empty = true;

  public SafeAppendable(Appendable a) {
    this.appendable = a;
  }

  public SafeAppendable append(CharSequence s) {
    try {
      if (empty && s.length() > 0) {
        empty = false;
      }
      appendable.append(s);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  public boolean isEmpty() {
    return empty;
  }

}

package org.apache.ibatis.session;

public class RowBounds {

  public final static int NO_ROW_OFFSET = 0;
  public final static int NO_ROW_LIMIT = Integer.MAX_VALUE;
  public final static RowBounds DEFAULT = new RowBounds();

  private int offset;
  private int limit;

  public RowBounds() {
    this.offset = NO_ROW_OFFSET;
    this.limit = NO_ROW_LIMIT;
  }

  public RowBounds(int offset, int limit) {
    this.offset = offset;
    this.limit = limit;
  }

  public int getOffset() {
    return offset;
  }

  public int getLimit() {
    return limit;
  }

}

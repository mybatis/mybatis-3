package com.ibatis.sqlmap.engine.transaction;

public abstract class BaseTransaction implements Transaction {
  private boolean commitRequired;

  public boolean isCommitRequired() {
    return commitRequired;
  }

  public void setCommitRequired(boolean commitRequired) {
    this.commitRequired = commitRequired;
  }

}

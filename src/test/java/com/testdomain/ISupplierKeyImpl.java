package com.testdomain;

/**
 * Used in testing the ResultObjectFactory
 *
 * @author Jeff Butler
 */
public class ISupplierKeyImpl implements ISupplierKey {

  private Integer supplierId;

  /**
   *
   */
  public ISupplierKeyImpl() {
    super();
  }

  public Integer getSupplierId() {
    return supplierId;
  }

  public void setSupplierId(Integer supplierId) {
    this.supplierId = supplierId;
  }

}

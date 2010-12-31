package org.apache.ibatis.submitted.refcursor;

public class OrderDetail {
    private Integer orderNumber;
    private Integer lineNumber;
    private Integer quantity;
    private String description;
    public Integer getLineNumber() {
        return lineNumber;
    }
    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }
    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Integer getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }
}

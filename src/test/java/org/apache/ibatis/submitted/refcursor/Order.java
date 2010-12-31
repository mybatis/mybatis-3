package org.apache.ibatis.submitted.refcursor;

import java.util.List;

public class Order {
    private Integer orderId;
    private String customerName;
    private List<OrderDetail> detailLines;
    public Integer getOrderId() {
        return orderId;
    }
    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }
    public String getCustomerName() {
        return customerName;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public List<OrderDetail> getDetailLines() {
        return detailLines;
    }
    public void setDetailLines(List<OrderDetail> detailLines) {
        this.detailLines = detailLines;
    }
}

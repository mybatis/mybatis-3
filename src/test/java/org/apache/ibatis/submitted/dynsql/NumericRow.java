package org.apache.ibatis.submitted.dynsql;

import java.math.BigDecimal;
import java.math.BigInteger;


public class NumericRow {
    private Integer id;
    private Byte tinynumber;
    private Short smallnumber;
    private Long longinteger;
    private BigInteger biginteger;
    private BigDecimal numericnumber;
    private BigDecimal decimalnumber;
    private Float realnumber;
    private Float floatnumber;
    private Double doublenumber;
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Byte getTinynumber() {
        return tinynumber;
    }
    public void setTinynumber(Byte tinynumber) {
        this.tinynumber = tinynumber;
    }
    public Short getSmallnumber() {
        return smallnumber;
    }
    public void setSmallnumber(Short smallnumber) {
        this.smallnumber = smallnumber;
    }
    public Long getLonginteger() {
        return longinteger;
    }
    public void setLonginteger(Long longinteger) {
        this.longinteger = longinteger;
    }
    public BigInteger getBiginteger() {
        return biginteger;
    }
    public void setBiginteger(BigInteger biginteger) {
        this.biginteger = biginteger;
    }
    public BigDecimal getNumericnumber() {
        return numericnumber;
    }
    public void setNumericnumber(BigDecimal numericnumber) {
        this.numericnumber = numericnumber;
    }
    public BigDecimal getDecimalnumber() {
        return decimalnumber;
    }
    public void setDecimalnumber(BigDecimal decimalnumber) {
        this.decimalnumber = decimalnumber;
    }
    public Float getRealnumber() {
        return realnumber;
    }
    public void setRealnumber(Float realnumber) {
        this.realnumber = realnumber;
    }
    public Float getFloatnumber() {
        return floatnumber;
    }
    public void setFloatnumber(Float floatnumber) {
        this.floatnumber = floatnumber;
    }
    public Double getDoublenumber() {
        return doublenumber;
    }
    public void setDoublenumber(Double doublenumber) {
        this.doublenumber = doublenumber;
    }
}

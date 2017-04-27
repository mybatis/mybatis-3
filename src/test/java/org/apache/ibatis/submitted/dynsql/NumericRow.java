/**
 *    Copyright 2009-2015 the original author or authors.
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

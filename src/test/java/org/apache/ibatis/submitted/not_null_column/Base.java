package org.apache.ibatis.submitted.not_null_column;

import java.util.Date;

public abstract class Base {
	private int tempIntField;
	private Date tempDateField;
	public int getTempIntField() {
		return tempIntField;
	}
	public void setTempIntField(int tempIntField) {
		this.tempIntField = tempIntField;
	}
	public Date getTempDateField() {
		return tempDateField;
	}
	public void setTempDateField(Date tempDateField) {
		this.tempDateField = tempDateField;
	}
}

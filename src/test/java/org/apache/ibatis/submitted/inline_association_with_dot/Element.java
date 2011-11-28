package org.apache.ibatis.submitted.inline_association_with_dot;

public class Element {
	private Element element;

	private String value;

	public Element getElement() {
		return element;
	}

	public void setElement(Element anElement) {
		element = anElement;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String aValue) {
		value = aValue;
	}
}

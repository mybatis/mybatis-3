package org.apache.ibatis.plugin.encryption.cryptogram;

public class CryptogramFailException extends Exception {

	private static final long serialVersionUID = 2481561706854845251L;

	public CryptogramFailException(String message) {
		super(message);
	}

}

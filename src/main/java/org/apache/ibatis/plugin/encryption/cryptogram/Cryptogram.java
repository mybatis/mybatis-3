package org.apache.ibatis.plugin.encryption.cryptogram;

public interface Cryptogram {
	public String encrypt(Object word)throws CryptogramFailException;

	public String decrypt(Object word)throws CryptogramFailException;
}

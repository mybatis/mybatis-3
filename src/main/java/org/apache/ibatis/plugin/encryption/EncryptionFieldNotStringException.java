package org.apache.ibatis.plugin.encryption;
/**
 * @author SoungRyoul Kim
 * Thank my mentor Ikchan Sim who taught me.
 */
public class EncryptionFieldNotStringException extends Exception{
	
	private static final long serialVersionUID = -781680570680579695L;

	public EncryptionFieldNotStringException(String message) {
		super(message);
	}

}

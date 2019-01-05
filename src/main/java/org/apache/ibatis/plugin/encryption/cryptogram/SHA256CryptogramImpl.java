package org.apache.ibatis.plugin.encryption.cryptogram;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256CryptogramImpl implements Cryptogram {

  private static final String algorithm = "SHA-256";

  @Override
  public String encrypt(Object word) throws CryptogramFailException {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new CryptogramFailException("there is no SHA-256 Algorithm");
    }

    md.update(((String) word).getBytes());

    StringBuilder sb = new StringBuilder();

    for (byte b : md.digest()) {

      sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));

    }

    return sb.toString();

  }

  @Override
  public String decrypt(Object word) throws CryptogramFailException {
    return (String) word;
  }

}

package org.apache.ibatis.plugin.encryption.cryptogram;


import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESCryptogramImpl implements Cryptogram {

  private static final String algorithm = "AES/CBC/PKCS5Padding";
  private String iv;
  private Key secretKey;
  private String encoding = "UTF-8";

  public AESCryptogramImpl(String key) {
    try {
      this.iv = key.substring(0, 16);
    } catch (StringIndexOutOfBoundsException e) {
      throw new StringIndexOutOfBoundsException(
          "SecretKey is too short, length required 16 at least");
    }
    byte[] keyBytes = new byte[16];
    byte[] b = null;
    try {
      b = key.getBytes(encoding);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    int len = b.length;
    if (len > keyBytes.length) {
      len = keyBytes.length;
    }
    System.arraycopy(b, 0, keyBytes, 0, len);
    SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

    this.secretKey = keySpec;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }


  @Override
  public String encrypt(Object word) throws CryptogramFailException {
    if (word == null) {
      return null;
    }

    Cipher c = null;
    byte[] encrypted = null;
    String encryptedWord = null;
    try {
      c = Cipher.getInstance(algorithm);

      c.init(Cipher.ENCRYPT_MODE, this.secretKey, new IvParameterSpec(iv.getBytes()));

      encrypted = c.doFinal(((String) word).getBytes(encoding));
      Base64.Encoder encoder = Base64.getEncoder();
      encryptedWord = new String(encoder.encode(encrypted));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new CryptogramFailException("Cryptogram Implementation fail to encrypt");
    }

    return encryptedWord;
  }

  @Override
  public String decrypt(Object word) throws CryptogramFailException {
    if (word == null) {
      return null;
    }

    Cipher cipher = null;
    String decryptWord = null;
    try {
      cipher = Cipher.getInstance(algorithm);

      cipher.init(Cipher.DECRYPT_MODE, this.secretKey,
          new IvParameterSpec(this.iv.getBytes(encoding)));

      Base64.Decoder decoder = Base64.getDecoder();
      byte[] decrypted = decoder.decode(((String) word).getBytes());

      decryptWord = new String(cipher.doFinal(decrypted), encoding);

    } catch (Exception e) {
      e.printStackTrace();
      throw new CryptogramFailException("Cryptogram Implementation fail to decrypt");
    }

    return decryptWord;
  }

}

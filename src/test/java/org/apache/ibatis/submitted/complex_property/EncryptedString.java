package org.apache.ibatis.submitted.complex_property;

public class EncryptedString {
  private String encrypted;

  public EncryptedString() {
    setEncrypted(null);
  }

  public EncryptedString(String message) {
    this();

    //encrypt the message.
    setEncrypted(message);
  }

  public String decrypt() {
    return encrypted;
  }

  public String getEncrypted() {
    return encrypted;
  }

  public void setEncrypted(String arg) {
    this.encrypted = arg;
  }

}

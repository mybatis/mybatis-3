package domain.blog;

public class ComplexImmutableAuthorId {
  protected final int id;
  protected final String email;
  protected final String username;
  protected final String password;

  public ComplexImmutableAuthorId(int aId, String aEmail, String aUsername, String aPassword) {
    id = aId;
    email = aEmail;
    username = aUsername;
    password = aPassword;
  }

  public int getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ComplexImmutableAuthorId that = (ComplexImmutableAuthorId) o;

    if (id != that.id) {
      return false;
    }
    if (email != null ? !email.equals(that.email) : that.email != null) {
      return false;
    }
    if (password != null ? !password.equals(that.password) : that.password != null) {
      return false;
    }
    if (username != null ? !username.equals(that.username) : that.username != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int myresult = id;
    myresult = 31 * myresult + (email != null ? email.hashCode() : 0);
    myresult = 31 * myresult + (username != null ? username.hashCode() : 0);
    myresult = 31 * myresult + (password != null ? password.hashCode() : 0);
    return myresult;
  }
}

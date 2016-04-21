package trunk.java.utils.test.http;

public class GrantRequest {
  private String grantType;
  private String username;
  private String password;

  public GrantRequest(String grantType, String username, String password) {
    this.grantType = grantType;
    this.username = username;
    this.password = password;
  }

  public String getPassword() {
    return password;
  }

  public String getGrantType() {
    return grantType;
  }

  public String getUsername() {
    return username;
  }
}

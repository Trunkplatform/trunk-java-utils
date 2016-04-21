package trunk.java.utils.test.http;

import java.util.List;

public class OAuthUser {
  private final String username = "test";
  private final int expires_in_seconds = 601422;
  private final int created_at = 1451436326;
  private final String resource_owner_type = "User";
  private final List<Integer> groups;
  private final List<String> permissions;
  private final List<Role> roles;
  private final boolean is_robot;

  public OAuthUser(List<Integer> groups, List<String> permissions, List<Role> roles, boolean is_robot) {
    this.groups = groups;
    this.permissions = permissions;
    this.roles = roles;
    this.is_robot = is_robot;
  }

  public static class Role {
    private final String role_name;
    private final int group_id;

    public Role(String role_name, int group_id) {
      this.role_name = role_name;
      this.group_id = group_id;
    }
  }
}

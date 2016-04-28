package com.trunk.test.http;

import java.util.List;

public class OAuthUser {
  private final String username;
  private final int expires_in_seconds;
  private final int created_at;
  private final String resource_owner_type;
  private final List<Integer> groups;
  private final List<String> permissions;
  private final List<Role> roles;
  private final boolean is_robot;

  public OAuthUser(int created_at,
                   String username,
                   int expires_in_seconds,
                   String resource_owner_type,
                   List<Integer> groups,
                   List<String> permissions,
                   List<Role> roles,
                   boolean is_robot) {
    this.created_at = created_at;
    this.username = username;
    this.expires_in_seconds = expires_in_seconds;
    this.resource_owner_type = resource_owner_type;
    this.groups = groups;
    this.permissions = permissions;
    this.roles = roles;
    this.is_robot = is_robot;
  }

  public OAuthUser(List<Integer> groups,
                   List<String> permissions,
                   List<Role> roles,
                   boolean is_robot) {
    this(1451436326, "test", 601422, "User", groups, permissions, roles, is_robot);
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

package com.trunk.test.http;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

public class GrantRequest {
  private String grantType;
  private String username;
  private String password;

  public static GrantRequest from(ByteBuf content) {
    String json = content.toString(CharsetUtil.UTF_8);
    JsonObject o = new JsonParser().parse(json).getAsJsonObject();
    return new GrantRequest(o.get("grant_type").getAsString(),
                            o.get("username").getAsString(),
                            o.get("password").getAsString());
  }

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

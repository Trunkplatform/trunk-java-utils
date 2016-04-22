package com.trunk.java.utils.test.http;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import io.netty.handler.codec.http.HttpResponseStatus;

public class GrantResponse {

  private String accessToken;
  private String tokenType;
  private int expiresIn;
  private int createdAt;
  private HttpResponseStatus statusCode;

  public GrantResponse(String accessToken, String tokenType, int expiresInSeconds, int createdAt) {
    this(accessToken, tokenType, expiresInSeconds, createdAt, HttpResponseStatus.OK);
  }

  public GrantResponse(String accessToken, String tokenType, int expiresIn, int createdAt, HttpResponseStatus statusCode) {
    this.accessToken = accessToken;
    this.tokenType = tokenType;
    this.expiresIn = expiresIn;
    this.createdAt = createdAt;
    this.statusCode = statusCode;
  }

  public HttpResponseStatus getStatusCode() {
    return statusCode;
  }

  public String asJson() {
    return new Gson().toJson(ImmutableMap.of("access_token", accessToken, "token_type", tokenType, "expires_in", expiresIn, "created_at", createdAt));
  }
}

package com.api.gateway.service;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;

public interface RestService {

  String getResponseString(String method, String url, MultiValueMap<String, String> headers,
      Map<String, Object> body);

  public <T> T get(String url, HttpEntity<?> entity, Class<T> responseType);

  public <T> T post(String url, HttpEntity<?> entity, Class<T> responseType);

}

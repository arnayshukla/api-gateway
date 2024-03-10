package com.api.gateway.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.api.gateway.service.RestService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RestServiceImpl implements RestService {

  @Autowired
  private RestTemplate restTemplate;

  @Override
  public String getResponseString(String method, String url, MultiValueMap<String, String> headers,
      Map<String, Object> body) {
    try {
      RequestEntity<Map<String, Object>> requestEntity = new RequestEntity<Map<String, Object>>(
          body, headers, HttpMethod.valueOf(method), new URI(url));
      ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);
      return response.getBody();
    } catch (URISyntaxException e) {
      log.error("Error executing API with url: {}", url);
      throw new RuntimeException(e.getMessage());
    }
  }

  public <T> T callApi(String url, HttpMethod method, HttpEntity<?> entity, Class<T> responseType) {
    return restTemplate.exchange(url, method, entity, responseType).getBody();
  }

  @Override
  public <T> T get(String url, HttpEntity<?> entity, Class<T> responseType) {
    return restTemplate.exchange(url, HttpMethod.GET, entity, responseType).getBody();
  }

  @Override
  public <T> T post(String url, HttpEntity<?> entity, Class<T> responseType) {
    return restTemplate.exchange(url, HttpMethod.POST, entity, responseType).getBody();
  }

}

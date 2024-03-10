package com.api.gateway.service.impl;

import java.util.Base64;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.api.gateway.dto.FilterConfig;
import com.api.gateway.enums.AuthenticationType;
import com.api.gateway.service.AuthService;
import com.api.gateway.service.RestService;
import com.api.gateway.util.Constants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AuthServiceImpl implements AuthService {

  @Autowired
  private RestService restService;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public String getAuthHeaderValue(FilterConfig config) {
    StringBuilder header = new StringBuilder();
    switch (AuthenticationType.valueOf(config.getKey())) {
      case BASIC_AUTH:
        header.append(Constants.Authentication.BASIC).append(Base64.getEncoder()
            .encodeToString((String.valueOf(config.getArgs().get(Constants.Authentication.USERNAME))
                + Constants.Symbols.COLON
                + String.valueOf(config.getArgs().get(Constants.Authentication.PASSWORD)))
                    .getBytes()));

        break;

      case JWT:
        header.append(Constants.Authentication.BEARER)
            .append(String.valueOf(config.getArgs().get(Constants.Authentication.TOKEN)));
        break;

      case JWT_API:
        String method = String.valueOf(config.getArgs().get(Constants.Authentication.METHOD));
        String url = String.valueOf(config.getArgs().get(Constants.Authentication.URL));
        Map<String, String> maps = objectMapper.convertValue(
            config.getArgs().get(Constants.Authentication.REQUEST_HEADERS),
            new TypeReference<Map<String, String>>() {});
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.setAll(maps);
        Map<String, Object> body =
            objectMapper.convertValue(config.getArgs().get(Constants.Authentication.REQUEST_BODY),
                new TypeReference<Map<String, Object>>() {});
        String token = restService.getResponseString(method, url, headers, body);
        header.append(Constants.Authentication.BEARER).append(token);
        break;

      default:
        throw new IllegalArgumentException("Unexpected value: " + config.getValue());
    }
    return header.toString();
  }

}

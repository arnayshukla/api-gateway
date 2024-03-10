package com.api.gateway.service;

import com.api.gateway.dto.FilterConfig;

public interface AuthService {

  public String getAuthHeaderValue(FilterConfig config);

}

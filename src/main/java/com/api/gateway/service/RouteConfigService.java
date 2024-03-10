package com.api.gateway.service;

import com.api.gateway.dto.RouteConfig;
import reactor.core.publisher.Flux;

public interface RouteConfigService {

  Flux<RouteConfig> getRouteConfig();

}

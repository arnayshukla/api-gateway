package com.api.gateway.filters;

import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import com.api.gateway.dto.FilterConfig;
import com.api.gateway.dto.RouteConfig;
import com.api.gateway.service.AuthService;
import com.api.gateway.util.CommonUtils;
import com.api.gateway.util.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
public class AuthenticationGatewayFilterFactory
    extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

  @Autowired
  private AuthService authService;

  @Data
  @NoArgsConstructor
  public static class Config {
    RouteConfig routeConfig;
    String name = Constants.Filter.AUTHENTICATION;

    public Config(RouteConfig routeConfig) {
      this.routeConfig = routeConfig;
    }
  }

  public AuthenticationGatewayFilterFactory() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      Optional<FilterConfig> filterConfig =
          CommonUtils.getPassthroughConfig(exchange, config.getRouteConfig(), config.getName());
      if (filterConfig.isEmpty()) {
        return chain.filter(exchange);
      }
      if (Objects
          .isNull(config.getRouteConfig().getMetadata().get(Constants.Authentication.AUTH_TOKEN))) {
        String authToken = authService.getAuthHeaderValue(filterConfig.get());
        config.getRouteConfig().getMetadata().put(Constants.Authentication.AUTH_TOKEN, authToken);
      }
      String finalToken = String
          .valueOf(config.getRouteConfig().getMetadata().get(Constants.Authentication.AUTH_TOKEN));
      ServerHttpRequest request = exchange.getRequest().mutate()
          .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, finalToken)).build();
      return chain.filter(exchange.mutate().request(request).build());
    };
  }

}

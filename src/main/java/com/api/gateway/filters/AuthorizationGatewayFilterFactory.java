package com.api.gateway.filters;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ServerWebExchange;
import com.api.gateway.config.ApplicationProperties.URL;
import com.api.gateway.dto.ComponentDTO;
import com.api.gateway.dto.FilterConfig;
import com.api.gateway.dto.RouteConfig;
import com.api.gateway.service.RestService;
import com.api.gateway.util.CommonUtils;
import com.api.gateway.util.Constants;
import com.api.gateway.util.Constants.Authorization;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@Component
public class AuthorizationGatewayFilterFactory
    extends AbstractGatewayFilterFactory<AuthorizationGatewayFilterFactory.Config> {

  @Data
  @NoArgsConstructor
  public static class Config {
    RouteConfig routeConfig;
    String name = Constants.Filter.AUTHORIZATION;

    public Config(RouteConfig routeConfig) {
      this.routeConfig = routeConfig;
    }
  }

  public AuthorizationGatewayFilterFactory() {
    super(Config.class);
  }

  @Autowired
  private RestService restService;

  @Autowired
  private URL urls;

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      Optional<FilterConfig> filterConfig =
          CommonUtils.getPassthroughConfig(exchange, config.getRouteConfig(), config.getName());
      if (filterConfig.isEmpty()) {
        return chain.filter(exchange);
      }
      String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

      if (StringUtils.isBlank(authHeader)) {
        return unauthorized(exchange, Authorization.HEADER_NOT_PRESENT);
      }

      String[] authHeaderParts = new String(Base64.getDecoder().decode(authHeader)).split(":");
      if (authHeaderParts.length != 2) {
        return unauthorized(exchange, Authorization.INVALID_HEADER);
      }
      String merchantKey = authHeaderParts[0];
      String merchantSecret = authHeaderParts[1];
      try {
        ComponentDTO componentResponse = getMerchantId(merchantSecret);
        if (!merchantKey.equals(componentResponse.getApiKey())) {
          return unauthorized(exchange, Authorization.INVALID_MERCHANT_KEY);
        }
        exchange =
            addHeader(exchange, Authorization.MERCHANT_ID, componentResponse.getMerchantId());
        return chain.filter(exchange);
      } catch (RestClientException e) {
        return unauthorized(exchange, e.getMessage());
      }
    };
  }

  private ComponentDTO getMerchantId(String merchantSecret) {
    String[] path = new String[] {"/", merchantSecret};
    HttpEntity<JsonNode> httpEntity = new HttpEntity<>(CommonUtils.getHeaders());
    String componentService = CommonUtils.addPath(urls.getComponentService(), path);
    return restService.get(componentService, httpEntity, ComponentDTO.class);
  }

  private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
    return setErrorCodeAndMessage(exchange, HttpStatus.UNAUTHORIZED, message);
  }

  private Mono<Void> setErrorCodeAndMessage(ServerWebExchange exchange, HttpStatus code,
      String message) {
    exchange.getResponse().setStatusCode(code);
    exchange = addHeader(exchange, "WWW-Authenticate", "Bearer");
    return writeErrorMessage(exchange, message);
  }

  private ServerWebExchange addHeader(ServerWebExchange exchange, String key, String value) {
    return exchange.mutate().request(exchange.getRequest().mutate().header(key, value).build())
        .build();
  }

  private Mono<Void> writeErrorMessage(ServerWebExchange exchange, String errorMessage) {
    DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
    DataBuffer buffer = bufferFactory.wrap(errorMessage.getBytes(StandardCharsets.UTF_8));
    return exchange.getResponse().writeWith(Mono.just(buffer));
  }
}

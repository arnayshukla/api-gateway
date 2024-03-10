package com.api.gateway.filters;

import java.util.Optional;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.SetRequestHeaderGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.api.gateway.dto.FilterConfig;
import com.api.gateway.dto.RouteConfig;
import com.api.gateway.util.CommonUtils;
import com.api.gateway.util.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@Component
public class SetRequestHeaderFilterFactory
    extends AbstractGatewayFilterFactory<SetRequestHeaderFilterFactory.Config> {
  @Data
  @NoArgsConstructor
  public static class Config {

    RouteConfig routeConfig;
    String name = Constants.Filter.SET_REQUEST_HEADER;

    public Config(RouteConfig routeConfig) {
      this.routeConfig = routeConfig;
    }

  }

  public SetRequestHeaderFilterFactory(
      SetRequestHeaderGatewayFilterFactory setRequestHeaderGatewayFilterFactory) {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return new GatewayFilter() {
      @Override
      public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Optional<FilterConfig> filterConfigOptional =
            CommonUtils.getPassthroughConfig(exchange, config.getRouteConfig(), config.getName());
        if (filterConfigOptional.isEmpty()) {
          return chain.filter(exchange);
        }
        FilterConfig filterConfig = filterConfigOptional.get();
        String value = ServerWebExchangeUtils.expand(exchange, filterConfig.getValue());
        ServerHttpRequest request = exchange.getRequest().mutate()
            .headers(httpHeaders -> httpHeaders.set(filterConfig.getKey(), value)).build();

        return chain.filter(exchange.mutate().request(request).build());
      }
    };
  }

}

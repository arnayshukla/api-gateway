package com.api.gateway.filters;

import java.util.Optional;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.SetResponseHeaderGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
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
public class SetResponseHeaderFilterFactory
    extends AbstractGatewayFilterFactory<SetResponseHeaderFilterFactory.Config> {

  @Data
  @NoArgsConstructor
  public static class Config {

    RouteConfig routeConfig;
    String name = Constants.Filter.SET_RESPONSE_HEADER;

    public Config(RouteConfig routeConfig) {
      this.routeConfig = routeConfig;
    }

  }

  public SetResponseHeaderFilterFactory(
      SetResponseHeaderGatewayFilterFactory setResponseHeaderGatewayFilterFactory) {
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
        String[] key = filterConfig.getKey().split(Constants.Symbols.COMMA);
        String[] value = ServerWebExchangeUtils.expand(exchange, filterConfig.getValue())
            .split(Constants.Symbols.COMMA);
        for (int i = 0; i < key.length; i++) {
          exchange.getResponse().getHeaders().add(key[i], value[i]);
        }
        return chain.filter(exchange);
      }
    };
  }

}

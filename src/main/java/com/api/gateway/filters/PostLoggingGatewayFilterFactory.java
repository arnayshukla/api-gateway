package com.api.gateway.filters;

import java.util.Optional;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import com.api.gateway.dto.FilterConfig;
import com.api.gateway.dto.RouteConfig;
import com.api.gateway.util.CommonUtils;
import com.api.gateway.util.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class PostLoggingGatewayFilterFactory
    extends AbstractGatewayFilterFactory<PostLoggingGatewayFilterFactory.Config> {

  @Data
  @NoArgsConstructor
  public static class Config {
    RouteConfig routeConfig;
    String name = Constants.Filter.POST_LOGGING;

    public Config(RouteConfig routeConfig) {
      this.routeConfig = routeConfig;
    }
  }

  public PostLoggingGatewayFilterFactory() {
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
      return chain.filter(exchange).then(Mono.fromRunnable(() -> {
        log.info("Request Completed for url {}", exchange.getRequest().getURI());
      }));
    };
  }

}

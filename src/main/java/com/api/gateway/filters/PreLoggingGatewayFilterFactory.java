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

@Component
@Slf4j
public class PreLoggingGatewayFilterFactory
    extends AbstractGatewayFilterFactory<PreLoggingGatewayFilterFactory.Config> {

  @Data
  @NoArgsConstructor
  public static class Config {
    RouteConfig routeConfig;

    String name = Constants.Filter.PRE_LOGGING;

    public Config(RouteConfig routeConfig) {
      this.routeConfig = routeConfig;
    }
  }

  public PreLoggingGatewayFilterFactory() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      Optional<FilterConfig> filterConfig =
          CommonUtils.getPassthroughConfig(exchange, config.getRouteConfig(), config.getName());
      if (filterConfig.isPresent()) {
        log.info("Request Received for url {}", exchange.getRequest().getURI());
      }
      return chain.filter(exchange);
    };
  }

}

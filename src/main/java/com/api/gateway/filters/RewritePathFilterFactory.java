package com.api.gateway.filters;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;
import java.util.Optional;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.SetPathGatewayFilterFactory;
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
public class RewritePathFilterFactory
    extends AbstractGatewayFilterFactory<RewritePathFilterFactory.Config> {

  @Data
  @NoArgsConstructor
  public static class Config {

    RouteConfig routeConfig;

    String name = Constants.Filter.REWRITE_PATH;

    public Config(RouteConfig routeConfig) {
      this.routeConfig = routeConfig;
    }

  }

  public RewritePathFilterFactory(SetPathGatewayFilterFactory setPathGatewayFilterFactory) {
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
        String replacementConfig = String.valueOf(filterConfig.getValue());
        String replacement = replacementConfig.replace("$\\", "$");
        ServerHttpRequest req = exchange.getRequest();
        addOriginalRequestUrl(exchange, req.getURI());
        String path = req.getURI().getRawPath();
        String regexpConfig = String.valueOf(filterConfig.getKey());
        String newPath = path.replaceAll(regexpConfig, replacement);
        ServerHttpRequest request = req.mutate().path(newPath).build();

        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, request.getURI());
        // exchange.getResponse().getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
        // "access-control-allow-origin,authorization,content-type,x-partner-code");
        // exchange.getResponse().getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        // exchange.getResponse().getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
        // "GET,POST,PUT,DELETE,OPTIONS,HEAD");

        return chain.filter(exchange.mutate().request(request).build());
      }
    };
  }

}

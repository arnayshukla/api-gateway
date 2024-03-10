package com.api.gateway.filters;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.getUriTemplateVariables;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriTemplate;
import com.api.gateway.dto.FilterConfig;
import com.api.gateway.dto.RouteConfig;
import com.api.gateway.enums.ModificationType;
import com.api.gateway.service.ModificationService;
import com.api.gateway.util.CommonUtils;
import com.api.gateway.util.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@Component
public class UrlModificationGatewayFilterFactory
    extends AbstractGatewayFilterFactory<UrlModificationGatewayFilterFactory.Config> {

  @Autowired
  private ModificationService modificationService;

  @Data
  @NoArgsConstructor
  public static class Config {

    RouteConfig routeConfig;

    String name = Constants.Filter.URL_MODIFICATION;

    public Config(RouteConfig routeConfig) {
      this.routeConfig = routeConfig;
    }

  }

  public UrlModificationGatewayFilterFactory() {
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
        ServerHttpRequest request = exchange.getRequest();

        String template = modificationService.processTemplate(request, exchange,
            ModificationType.valueOf(
                String.valueOf(filterConfig.getArgs().get(Constants.Template.MODIFICATION_TYPE))),
            String.valueOf(filterConfig.getArgs().get(Constants.Template.TEMPLATE_DIR)),
            String.valueOf(filterConfig.getArgs().get(Constants.Template.TEMPLATE_NAME)));
        addOriginalRequestUrl(exchange, request.getURI());
        UriTemplate uriTemplate = new UriTemplate(template);
        Map<String, String> uriVariables = getUriTemplateVariables(exchange);

        URI uri = uriTemplate.expand(uriVariables);
        String newPath = uri.getRawPath();

        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, uri);

        ServerHttpRequest mutatedRequest = request.mutate().path(newPath).build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
      }
    };
  }

}

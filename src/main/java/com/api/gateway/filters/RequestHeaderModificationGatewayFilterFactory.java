package com.api.gateway.filters;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import com.api.gateway.dto.FilterConfig;
import com.api.gateway.dto.RouteConfig;
import com.api.gateway.enums.ModificationType;
import com.api.gateway.service.ModificationService;
import com.api.gateway.util.CommonUtils;
import com.api.gateway.util.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RequestHeaderModificationGatewayFilterFactory
    extends AbstractGatewayFilterFactory<RequestHeaderModificationGatewayFilterFactory.Config> {


  @Autowired
  private ModificationService modificationService;

  @Autowired
  private ObjectMapper objectMapper;

  @Data
  @NoArgsConstructor
  public static class Config {

    RouteConfig routeConfig;

    String name = Constants.Filter.REQUEST_HEADER_MODIFICATION;

    public Config(RouteConfig routeConfig) {
      this.routeConfig = routeConfig;
    }

  }

  public RequestHeaderModificationGatewayFilterFactory() {
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
        ServerHttpRequest request = exchange.getRequest();
        FilterConfig filterConfig = filterConfigOptional.get();
        String template = modificationService.processTemplate(request, exchange,
            ModificationType.valueOf(
                String.valueOf(filterConfig.getArgs().get(Constants.Template.MODIFICATION_TYPE))),
            String.valueOf(filterConfig.getArgs().get(Constants.Template.TEMPLATE_DIR)),
            String.valueOf(filterConfig.getArgs().get(Constants.Template.TEMPLATE_NAME)));
        try {
          Map<String, String> headers = objectMapper.readValue(template, Map.class);
          MultiValueMap<String, String> multiValueHeader = new LinkedMultiValueMap<>();
          for (Entry<String, String> header : headers.entrySet()) {
            multiValueHeader.add(header.getKey(), header.getValue());
          }
          ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
              .headers(httpHeaders -> httpHeaders.addAll(multiValueHeader)).build();
          return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (JsonProcessingException e) {
          log.error("Exception Occured during request header modification: {}", e.getMessage());
          throw new RuntimeException("Exception Occured during request header modification");
        }
      }
    };

  }


}

package com.api.gateway.filters;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.api.gateway.dto.FilterConfig;
import com.api.gateway.dto.RouteConfig;
import com.api.gateway.enums.ModificationType;
import com.api.gateway.service.ModificationService;
import com.api.gateway.util.CommonUtils;
import com.api.gateway.util.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ResponseBodyModificationFilterFactory
    extends AbstractGatewayFilterFactory<ResponseBodyModificationFilterFactory.Config> {

  @Autowired
  private ModificationService modificationService;

  @Data
  @NoArgsConstructor
  public static class Config {

    RouteConfig routeConfig;
    String name = Constants.Filter.RESPONSE_BODY_MODIFICATION;

    public Config(RouteConfig routeConfig) {
      this.routeConfig = routeConfig;
    }

  }

  public ResponseBodyModificationFilterFactory() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    GatewayFilter gatewayFilter = new GatewayFilter() {

      @Override
      public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Optional<FilterConfig> filterConfigOptional =
            CommonUtils.getPassthroughConfig(exchange, config.getRouteConfig(), config.getName());
        if (filterConfigOptional.isEmpty()) {
          return chain.filter(exchange);
        }
        FilterConfig filterConfig = filterConfigOptional.get();
        ModificationType modificationType = ModificationType.valueOf(
            String.valueOf(filterConfig.getArgs().get(Constants.Template.MODIFICATION_TYPE)));
        ServerHttpResponse response = exchange.getResponse();
        ServerHttpResponse modifiedResponse =
            getDecoratedResponse(response, filterConfig, modificationType);
        return chain.filter(
            exchange.mutate().request(exchange.getRequest()).response(modifiedResponse).build());
      }

      private ServerHttpResponseDecorator getDecoratedResponse(ServerHttpResponse response,
          FilterConfig filterConfig, ModificationType modificationType) {
        ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(response) {

          @Override
          public Mono<Void> writeWith(final Publisher<? extends DataBuffer> body) {

            if (body instanceof Flux) {

              Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

              return super.writeWith(fluxBody.buffer().map(dataBuffers -> {

                DefaultDataBuffer joinedBuffers = new DefaultDataBufferFactory().join(dataBuffers);
                byte[] content = new byte[joinedBuffers.readableByteCount()];
                joinedBuffers.read(content);
                String responseBody = new String(content, StandardCharsets.UTF_8);
                String modifiedBody = modificationService.apply(responseBody, modificationType,
                    String.valueOf(filterConfig.getArgs().get(Constants.Template.TEMPLATE_DIR)),
                    String.valueOf(filterConfig.getArgs().get(Constants.Template.TEMPLATE_NAME)));
                DataBuffer result = response.bufferFactory().wrap(modifiedBody.getBytes());
                MediaType contentType = ((modificationType.equals(ModificationType.JSON_TO_JSON)
                    || modificationType.equals(ModificationType.XML_TO_JSON))
                        ? MediaType.APPLICATION_JSON
                        : MediaType.APPLICATION_XML);
                response.getHeaders().setContentType(contentType);
                response.getHeaders().setContentLength(result.readableByteCount());
                return result;
              })).onErrorResume(err -> {
                return Mono.empty();
              });

            }
            return super.writeWith(body);
          }
        };
        return responseDecorator;
      }

    };
    return new OrderedGatewayFilter(gatewayFilter, -4);
  }
}

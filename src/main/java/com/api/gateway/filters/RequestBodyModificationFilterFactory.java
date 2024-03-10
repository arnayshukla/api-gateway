package com.api.gateway.filters;

import java.util.Optional;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import com.api.gateway.dto.FilterConfig;
import com.api.gateway.dto.RouteConfig;
import com.api.gateway.enums.ModificationType;
import com.api.gateway.service.ModificationService;
import com.api.gateway.util.CommonUtils;
import com.api.gateway.util.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RequestBodyModificationFilterFactory
    extends AbstractGatewayFilterFactory<RequestBodyModificationFilterFactory.Config> {

  // @Autowired
  // private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

  @Autowired
  private ModificationService modificationService;


  @Data
  @NoArgsConstructor
  public static class Config {

    RouteConfig routeConfig;
    String name = Constants.Filter.REQUEST_BODY_MODIFICATION;

    public Config(RouteConfig routeConfig) {
      this.routeConfig = routeConfig;
    }

  }

  public RequestBodyModificationFilterFactory() {
    super(Config.class);
  }

  @Override
  @SuppressWarnings("unchecked")
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
        ModificationType modificationType = ModificationType.valueOf(
            String.valueOf(filterConfig.getArgs().get(Constants.Template.MODIFICATION_TYPE)));

        Class inClass = String.class;
        Class outClass = String.class;
        ServerRequest serverRequest =
            ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());

        // TODO: flux or mono
        Mono<?> modifiedBody = serverRequest.bodyToMono(inClass)
            .flatMap(originalBody -> modificationService.apply(originalBody, modificationType,
                String.valueOf(filterConfig.getArgs().get(Constants.Template.TEMPLATE_DIR)),
                String.valueOf(filterConfig.getArgs().get(Constants.Template.TEMPLATE_NAME))));

        BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, outClass);
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(exchange.getRequest().getHeaders());

        // the new content type will be computed by bodyInserter
        // and then set in the request decorator
        headers.remove(HttpHeaders.CONTENT_LENGTH);

        // if the body is changing content types, set it here, to the bodyInserter
        // will know about it
        String contentType = ((modificationType.equals(ModificationType.JSON_TO_JSON)
            || modificationType.equals(ModificationType.XML_TO_JSON))
                ? MediaType.APPLICATION_JSON_VALUE
                : MediaType.APPLICATION_XML_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, contentType);
        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
        return bodyInserter.insert(outputMessage, new BodyInserterContext())
            // .log("modify_request", Level.INFO)
            .then(Mono.defer(() -> {
              ServerHttpRequest decorator = decorate(exchange, headers, outputMessage);
              return chain.filter(exchange.mutate().request(decorator).build());
            })).onErrorResume((Function<Throwable, Mono<Void>>) throwable -> release(exchange,
                outputMessage, throwable));
      }
    };
  }

  protected Mono<Void> release(ServerWebExchange exchange, CachedBodyOutputMessage outputMessage,
      Throwable throwable) {
    // if (outputMessage.isCached()) {
    // return outputMessage.getBody().map(DataBufferUtils::release).then(Mono.error(throwable));
    // }
    return Mono.error(throwable);
  }

  ServerHttpRequestDecorator decorate(ServerWebExchange exchange, HttpHeaders headers,
      CachedBodyOutputMessage outputMessage) {
    return new ServerHttpRequestDecorator(exchange.getRequest()) {
      @Override
      public HttpHeaders getHeaders() {
        long contentLength = headers.getContentLength();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.putAll(headers);
        if (contentLength > 0) {
          httpHeaders.setContentLength(contentLength);
        } else {
          // TODO: this causes a 'HTTP/1.1 411 Length Required' // on
          // httpbin.org
          httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
        }
        return httpHeaders;
      }

      @Override
      public Flux<DataBuffer> getBody() {
        return outputMessage.getBody();
      }
    };
  }

}

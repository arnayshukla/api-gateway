package com.api.gateway.service;

import java.util.Map;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import com.api.gateway.enums.ModificationType;
import reactor.core.publisher.Mono;

public interface ModificationService {

  public Mono<String> apply(Object input, ModificationType modificationType,
      String templateLocation, String templateName);

  public String apply(Map<String, Object> dataModel, String templateLocation, String templateName);

  public String processTemplate(ServerHttpRequest request, ServerWebExchange exchange,
      ModificationType modificationType, String templateLocation, String templateName);

  public String apply(String input, ModificationType modificationType, String templateLocation,
      String templateName);

}

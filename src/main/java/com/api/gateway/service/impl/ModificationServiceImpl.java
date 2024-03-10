package com.api.gateway.service.impl;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import com.api.gateway.enums.ModificationType;
import com.api.gateway.service.FreemarkerService;
import com.api.gateway.service.ModificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ModificationServiceImpl implements ModificationService {

  @Autowired
  private FreemarkerService freemarkerService;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private XmlMapper xmlMapper;

  @Override
  public Mono<String> apply(Object input, ModificationType modificationType,
      String templateLocation, String templateName) {

    return Mono.just(input instanceof String
        ? apply((String) input, modificationType, templateLocation, templateName)
        : StringUtils.EMPTY);
  }

  @Override
  public String apply(String input, ModificationType modificationType, String templateLocation,
      String templateName) {

    if (StringUtils.isBlank(input))
      return StringUtils.EMPTY;

    Map<String, Object> dataModel = getInputDataModel(input, modificationType);
    return apply(dataModel, templateLocation, templateName);
  }

  @Override
  public String apply(Map<String, Object> dataModel, String templateLocation, String templateName) {

    String output = freemarkerService.processTemplate(templateName, templateLocation, dataModel);
    return output;
  }

  private Map<String, Object> getInputDataModel(String input, ModificationType modificationType) {
    Map<String, Object> dataModel = new HashMap<>();
    switch (modificationType) {
      case XML_TO_XML:
      case XML_TO_JSON:
        try {
          dataModel = xmlMapper.readValue(input, Map.class);
        } catch (Exception e) {
          log.error("Unable to convert string to XML");
          // TODO Use Custom Exception Class
          throw new RuntimeException("Unable to convert string to XML");
        }
        break;

      case JSON_TO_JSON:
      case JSON_TO_XML:
        try {
          dataModel = objectMapper.readValue(input, Map.class);
        } catch (Exception e) {
          log.error("Unable to convert string to JSON");
          // TODO Use Custom Exception Class
          throw new RuntimeException("Unable to convert string to JSON");
        }
        break;
      default:
        log.error("Unsupported Modification Type: {}", modificationType);
        // TODO Use Custom Exception Class
        throw new RuntimeException("Unsupported Modification Type");
    }
    return dataModel;
  }

  @Override
  public String processTemplate(ServerHttpRequest request, ServerWebExchange exchange,
      ModificationType modificationType, String templateLocation, String templateName) {
    Map<String, Object> dataModel = new HashMap<>();
    if (!request.getHeaders().isEmpty())
      dataModel.putAll(request.getHeaders().toSingleValueMap());

    if (request.getMethod().equals(HttpMethod.GET) && !request.getQueryParams().isEmpty())
      dataModel.putAll(request.getQueryParams().toSingleValueMap());

    if (request.getMethod().equals(HttpMethod.POST) && !request.getQueryParams().isEmpty()) {
      String body = exchange.getAttribute("cachedRequestBodyObject");
      Map<String, Object> requestBody = getInputDataModel(body, modificationType);
      dataModel.putAll(requestBody);
    }
    return apply(dataModel, templateLocation, templateName);
  }
}

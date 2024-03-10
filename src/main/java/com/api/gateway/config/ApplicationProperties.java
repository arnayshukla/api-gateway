package com.api.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties
public class ApplicationProperties {

  @ConfigurationProperties(prefix = "url")
  @ConfigurationPropertiesScan
  @Data
  public class URL {
    private String componentService;
  }

}

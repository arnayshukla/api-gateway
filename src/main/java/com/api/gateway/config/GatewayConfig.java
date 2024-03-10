package com.api.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.api.gateway.filters.factory.FilterFactory;
import com.api.gateway.service.RouteConfigService;

@Configuration
public class GatewayConfig {

  @Bean
  RouteLocator routeLocator(RouteConfigService routeConfigService,
      RouteLocatorBuilder routeLocatorBuilder, FilterFactory filterFactory) {
    return new RouteLocatorImpl(routeConfigService, routeLocatorBuilder, filterFactory);
  }

}

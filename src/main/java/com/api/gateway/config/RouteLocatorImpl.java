package com.api.gateway.config;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.BooleanSpec;
import org.springframework.cloud.gateway.route.builder.Buildable;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.UriSpec;
import org.springframework.http.HttpMethod;
import com.api.gateway.dto.PredicateConfig;
import com.api.gateway.dto.RouteConfig;
import com.api.gateway.filters.factory.FilterFactory;
import com.api.gateway.service.RouteConfigService;
import com.api.gateway.util.Constants;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;

@AllArgsConstructor
public class RouteLocatorImpl implements RouteLocator {

  private final RouteConfigService routeConfigService;

  private final RouteLocatorBuilder routeLocatorBuilder;

  private final FilterFactory filterFactory;

  @Override
  public Flux<Route> getRoutes() {
    RouteLocatorBuilder.Builder routesBuilder = routeLocatorBuilder.routes();
    return routeConfigService.getRouteConfig()
        .map(routeConfig -> routesBuilder.route(routeConfig.getId(),
            predicateSpec -> setPredicateSpec(routeConfig, predicateSpec)))
        .collectList().flatMapMany(builders -> routesBuilder.build().getRoutes());
  }

  private Buildable<Route> setPredicateSpec(RouteConfig routeConfig, PredicateSpec predicateSpec) {
    PredicateConfig predicateConfig = routeConfig.getPredicate();
    BooleanSpec booleanSpec = predicateSpec.path(predicateConfig.getPath()).and()
        .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE).and()
        .host(predicateConfig.getHost()).and().header(Constants.Partner.X_PARTNER_CODE,
            predicateConfig.getHeader().get(Constants.Partner.X_PARTNER_CODE));
    booleanSpec.filters(gatewayFilterSpec -> setFilters(routeConfig, gatewayFilterSpec));

    return booleanSpec.uri(routeConfig.getUrl());
  }

  private UriSpec setFilters(RouteConfig routeConfig, GatewayFilterSpec gatewayFilterSpec) {
    filterFactory.setFilters(routeConfig, gatewayFilterSpec);
    return gatewayFilterSpec.metadata(routeConfig.getMetadata());

  }
}

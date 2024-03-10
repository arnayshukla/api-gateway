package com.api.gateway.filters.factory;

import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.stereotype.Component;
import com.api.gateway.dto.RouteConfig;
import com.api.gateway.filters.AuthenticationGatewayFilterFactory;
import com.api.gateway.filters.AuthorizationGatewayFilterFactory;
import com.api.gateway.filters.PostLoggingGatewayFilterFactory;
import com.api.gateway.filters.PreLoggingGatewayFilterFactory;
import com.api.gateway.filters.RequestBodyModificationFilterFactory;
import com.api.gateway.filters.RequestHeaderModificationGatewayFilterFactory;
import com.api.gateway.filters.ResponseBodyModificationFilterFactory;
import com.api.gateway.filters.RewritePathFilterFactory;
import com.api.gateway.filters.SetRequestHeaderFilterFactory;
import com.api.gateway.filters.SetResponseHeaderFilterFactory;
import com.api.gateway.filters.UrlModificationGatewayFilterFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FilterFactory {

  private final RewritePathFilterFactory rewritePathFilterFactory;

  private final PreLoggingGatewayFilterFactory preLoggingGatewayFilterFactory;

  private final PostLoggingGatewayFilterFactory postLoggingGatewayFilterFactory;

  private final RequestBodyModificationFilterFactory requestBodyModificationFilterFactory;

  private final ResponseBodyModificationFilterFactory responseBodyModificationFilterFactory;

  private final SetRequestHeaderFilterFactory setRequestHeaderFilterFactory;

  private final SetResponseHeaderFilterFactory setResponseHeaderFilterFactory;

  private final AuthenticationGatewayFilterFactory authenticationGatewayFilterFactory;

  private final AuthorizationGatewayFilterFactory authorizationGatewayFilterFactory;

  private final RequestHeaderModificationGatewayFilterFactory requestHeaderModificationGatewayFilterFactory;

  private final UrlModificationGatewayFilterFactory urlModificationGatewayFilterFactory;

  public void setFilters(RouteConfig routeConfig, GatewayFilterSpec gatewayFilterSpec) {
    gatewayFilterSpec.filters(preLoggingGatewayFilterFactory
        .apply(new PreLoggingGatewayFilterFactory.Config(routeConfig)));
    gatewayFilterSpec
        .filters(rewritePathFilterFactory.apply(new RewritePathFilterFactory.Config(routeConfig)));
    gatewayFilterSpec.filters(urlModificationGatewayFilterFactory
        .apply(new UrlModificationGatewayFilterFactory.Config(routeConfig)));
    gatewayFilterSpec.filters(requestHeaderModificationGatewayFilterFactory
        .apply(new RequestHeaderModificationGatewayFilterFactory.Config(routeConfig)));
    gatewayFilterSpec.filters(
        setRequestHeaderFilterFactory.apply(new SetRequestHeaderFilterFactory.Config(routeConfig)));
    gatewayFilterSpec.filters(authenticationGatewayFilterFactory
        .apply(new AuthenticationGatewayFilterFactory.Config(routeConfig)));
    gatewayFilterSpec.filters(authorizationGatewayFilterFactory
        .apply(new AuthorizationGatewayFilterFactory.Config(routeConfig)));
    gatewayFilterSpec.filters(requestBodyModificationFilterFactory
        .apply(new RequestBodyModificationFilterFactory.Config(routeConfig)));
    gatewayFilterSpec.filters(setResponseHeaderFilterFactory
        .apply(new SetResponseHeaderFilterFactory.Config(routeConfig)));
    gatewayFilterSpec.filters(responseBodyModificationFilterFactory
        .apply(new ResponseBodyModificationFilterFactory.Config(routeConfig)));
    gatewayFilterSpec.filters(postLoggingGatewayFilterFactory
        .apply(new PostLoggingGatewayFilterFactory.Config(routeConfig)));
  }
}

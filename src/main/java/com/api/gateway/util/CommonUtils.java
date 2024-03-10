package com.api.gateway.util;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.getUriTemplateVariables;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import com.api.gateway.dto.FilterConfig;
import com.api.gateway.dto.PassthroughConfig;
import com.api.gateway.dto.RouteConfig;

public class CommonUtils {

  public static Optional<FilterConfig> getPassthroughConfig(ServerWebExchange exchange,
      RouteConfig routeConfig, String filterName) {
    String segment = getUriTemplateVariables(exchange).get(Constants.SEGMENT);
    String method = exchange.getRequest().getMethod().name();
    String passthroughKey = StringUtils.join(method, Constants.Symbols.UNDERSCORE, segment);
    if (routeConfig.getPassthroughs().containsKey(passthroughKey)) {
      PassthroughConfig passthroughConfig = routeConfig.getPassthroughs().get(passthroughKey);
      if (passthroughConfig.getFilters().containsKey(filterName)) {
        FilterConfig filterConfig = passthroughConfig.getFilters().get(filterName);
        return Optional.ofNullable(filterConfig);
      }
    }
    return Optional.empty();
  }

  public static MultiValueMap<String, String> getHeaders() {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
    return headers;
  }

  public static String addPath(String url, String path) {
    StringBuilder sb = new StringBuilder(url);
    sb.append(path);

    return sb.toString();
  }

  public static String addPath(String url, String[] path) {
    StringBuilder sb = new StringBuilder(url);
    for (String p : path) {
      sb.append(p);
    }
    return sb.toString();
  }
}

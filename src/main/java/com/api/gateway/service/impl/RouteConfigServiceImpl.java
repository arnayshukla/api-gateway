package com.api.gateway.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import com.api.gateway.dto.PartnerConfig;
import com.api.gateway.dto.RouteConfig;
import com.api.gateway.service.RouteConfigService;
import com.api.gateway.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteConfigServiceImpl implements RouteConfigService {

  private final ObjectMapper objectMapper;

  private final Environment env;

  @Override
  public Flux<RouteConfig> getRouteConfig() {
    List<RouteConfig> routeConfigList = new ArrayList<>();
    try {
      PathMatchingResourcePatternResolver resourcePatternResolver =
          new PathMatchingResourcePatternResolver();
      String location = Constants.CONFIG_RESOURCE_LOCATION.replace(Constants.PROFILE_PLACEHOLDER,
          env.getProperty(Constants.DEPLOYMENT_TYPE).toLowerCase());
      // "egress");
      Resource[] resources = resourcePatternResolver.getResources(location);
      for (Resource resource : resources) {
        byte[] binaryData = FileCopyUtils.copyToByteArray(resource.getInputStream());
        resource.getInputStream().close();
        String config = new String(binaryData, StandardCharsets.UTF_8);
        PartnerConfig partnerConfig = objectMapper.readValue(config, PartnerConfig.class);
        log.info("adding config: {}", partnerConfig);
        routeConfigList.addAll(partnerConfig.getRoutes().stream().filter(route -> route.isStatus())
            .collect(Collectors.toList()));
      }
    } catch (IOException ex) {
      log.error("Cannot fetch config for wallet partners");
      // TODO Use Custom Exception Class
      throw new RuntimeException();
    }

    return Flux.fromIterable(routeConfigList);
  }

}

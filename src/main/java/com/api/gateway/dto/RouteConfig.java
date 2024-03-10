package com.api.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteConfig {

  @NotBlank
  private String id;

  private boolean status;

  @NotBlank
  private String url;

  @NotNull
  private PredicateConfig predicate;

  private Map<String, PassthroughConfig> passthroughs = new HashMap<>();

  private Map<String, Object> metadata = new HashMap<>();
}

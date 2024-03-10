package com.api.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredicateConfig {

  private String path;

  private String host;

  private String method;

  private Map<String, String> header = new HashMap<>();

  private Map<String, String> query = new HashMap<>();

  private Map<String, String> cookie = new HashMap<>();
}

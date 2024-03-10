package com.api.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterConfig {

  private String key;

  private String value;

  private Map<String, Object> args = new HashMap<>();
}

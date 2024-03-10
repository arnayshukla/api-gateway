package com.api.gateway.dto;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassthroughConfig {

  @NotEmpty
  private Map<String, FilterConfig> filters = new HashMap<>();

}

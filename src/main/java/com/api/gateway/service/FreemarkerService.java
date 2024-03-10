package com.api.gateway.service;

import java.util.Map;

public interface FreemarkerService {

  public String processTemplate(String templateName, String templateLocation,
      Map<String, Object> dataModel);

}

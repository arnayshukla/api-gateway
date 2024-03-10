package com.api.gateway.service.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.api.gateway.service.FreemarkerService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FreemarkerServiceImpl implements FreemarkerService {

  @Override
  public String processTemplate(String templateName, String templateLocation,
      Map<String, Object> dataModel) {
    try {
      Configuration freeMarkerConfiguration = new Configuration(Configuration.VERSION_2_3_31);
      freeMarkerConfiguration.setClassForTemplateLoading(this.getClass(), templateLocation);
      Template template = freeMarkerConfiguration.getTemplate(templateName);
      final Writer out = new StringWriter();
      template.process(dataModel, out);
      log.info("Template {} processed successfully", templateName);
      String output = out.toString();
      out.close();
      return output;
    } catch (IOException ex) {
      log.error("Exception occurred while processing template {}", templateName);
      // TODO Use Custom Exception Class
      throw new RuntimeException("Exception occurred while processing template");
    } catch (TemplateException e) {
      log.error("Unable to process FTL - {}", e.getTemplateSourceName());
      // TODO Use Custom Exception Class
      throw new RuntimeException("Unable to process FTL");
    }
  }
}

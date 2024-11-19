package com.epam.genai.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "client-openai")
public class OpenAiClientProperties {
  private String key;
  private String endpoint;
  private String model;
}

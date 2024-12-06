package com.epam.genai.rest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class InputRequest {
  public static final Double DEFAULT_TEMPERATURE = 0.5;
  private String input;
  private Double temperature;
  private String model;

  public Double getTemperature() {
    return temperature != null ? temperature : DEFAULT_TEMPERATURE;
  }
}
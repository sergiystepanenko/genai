package com.epam.genai.rest;

import static com.epam.genai.rest.model.InputRequest.DEFAULT_TEMPERATURE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.genai.rest.model.InputRequest;
import com.epam.genai.service.SimpleChatLlmService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class GenAiControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SimpleChatLlmService llmService;

  @Test
  void chatWithTemperature() {
    double temperature = 0.7;
    InputRequest inputRequest = InputRequest.builder()
        .input("Hello world")
        .temperature(temperature)
        .build();
    String expectedResponse = "This is a response from OpenAI";

    when(llmService.simpleChat(anyString(), eq(temperature), eq(null))).thenReturn(expectedResponse);

    performRestRequest(inputRequest, expectedResponse);
  }

  @Test
  void chatDefaultTemperature() {
    InputRequest inputRequest = InputRequest.builder()
        .input("Hello world")
        .build();
    String expectedResponse = "This is a response from OpenAI";

    when(llmService.simpleChat(anyString(), eq(DEFAULT_TEMPERATURE), eq(null))).thenReturn(expectedResponse);

    performRestRequest(inputRequest, expectedResponse);
  }

  @Test
  void chatEndpointWithModel() {
    String model = "llm model";
    InputRequest inputRequest = InputRequest.builder()
        .input("Hello world")
        .model(model)
        .build();
    String expectedResponse = "This is a response from OpenAI";

    when(llmService.simpleChat(anyString(), eq(DEFAULT_TEMPERATURE), eq(model))).thenReturn(expectedResponse);

    performRestRequest(inputRequest, expectedResponse);
  }

  @SneakyThrows
  private void performRestRequest(InputRequest inputRequest, String expectedResponse) {
    ObjectMapper objectMapper = new ObjectMapper();

    mockMvc.perform(post("/api/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(inputRequest)))
        .andExpect(status().isOk())
        .andExpect(content().string(expectedResponse));
  }
}
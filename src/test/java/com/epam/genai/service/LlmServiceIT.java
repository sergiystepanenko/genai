package com.epam.genai.service;

import com.epam.genai.GenAiApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {GenAiApplication.class})
class LlmServiceIT {

  @SpyBean
  private LlmService llmService;

  @ParameterizedTest
  @ValueSource(strings = {"gpt-4o", "gemini-pro"})
//gpt-4o, Mistral-7B-Instruct, Llama-3-8B-Instruct
  void chatLibrarian(String model) {
    String result1 = llmService.chatLibrarian("Hi!", model);
    Assertions.assertNotNull(result1);

    String result2 = llmService.chatLibrarian("""
        I love astrophysics and cosmology,
        I'd like to learn something new about black holes, any suggestion""", model);
    Assertions.assertNotNull(result2);

    String result3 = llmService.chatLibrarian("""
        Give me the best one from this list.
        Describe its content in single brief sentence.""", 1.0, model);
    Assertions.assertNotNull(result3);

    String result4 = llmService.chatLibrarian("Where can I buy this book?", model);
    Assertions.assertNotNull(result4);

    String result5 = llmService.chatLibrarian("Audio book, price max $10", model);
    Assertions.assertNotNull(result5);
  }
}
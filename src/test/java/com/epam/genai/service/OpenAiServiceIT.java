package com.epam.genai.service;

import com.epam.genai.GenAiApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {GenAiApplication.class})
public class OpenAiServiceIT {

  @SpyBean
  private OpenAiService openAiService;

  @Test
  public void chatLibrarian() {
    String result1 = openAiService.chatLibrarian("Hi!");
    Assertions.assertNotNull(result1);

    String result2 = openAiService.chatLibrarian("""
        I love astrophysics and cosmology,
        I'd like to learn something new about black holes, any suggestion""");
    Assertions.assertNotNull(result2);

    String result3 = openAiService.chatLibrarian("""
        Give me the best one from this list.
        Describe its content in single brief sentence.""", 1.0);
    Assertions.assertNotNull(result3);

    String result4 = openAiService.chatLibrarian("Where can I buy this book?");
    Assertions.assertNotNull(result4);

    String result5 = openAiService.chatLibrarian("Audio book, price max $10");
    Assertions.assertNotNull(result5);
  }
}
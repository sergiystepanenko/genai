package com.epam.genai.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.genai.GenAiApplication;
import com.epam.genai.service.semantickernal.BookPlugin;
import com.epam.genai.service.semantickernal.model.book.BookFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {GenAiApplication.class})
class ChatLibrarianLlmServiceIT {

  @SpyBean
  private ChatLibrarianLlmService chatLibrarianLlmService;

  @SpyBean
  private BookPlugin bookPlugin;


  @ParameterizedTest
  @ValueSource(strings = {"gpt-4o"})
//gpt-4o, gemini-pro, Mistral-7B-Instruct, Llama-3-8B-Instruct
  void chatLibrarian(String model) {
    String result1 = chatLibrarianLlmService.chatLibrarian("Hi!", model);
    Assertions.assertNotNull(result1);

    String result2 = chatLibrarianLlmService.chatLibrarian("""
        I love astrophysics and cosmology,
        I'd like to learn something new about black holes, any suggestion""", model);
    Assertions.assertNotNull(result2);

    String result3 = chatLibrarianLlmService.chatLibrarian("""
        Give me the best one from this list.
        Describe its content in single brief sentence.""", 1.0, model);
    Assertions.assertNotNull(result3);

    String result4 = chatLibrarianLlmService.chatLibrarian("Where can I buy this book?", model);
    Assertions.assertNotNull(result4);

    String result5 = chatLibrarianLlmService.chatLibrarian("Audio book, price max $10", model);

    verify(bookPlugin, times(1)).whereToBuyBook(BookFormat.AUDIOBOOK, 0.0, 10.0);

    Assertions.assertNotNull(result5);
  }
}
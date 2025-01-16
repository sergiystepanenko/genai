package com.epam.genai.rest;

import com.epam.genai.service.photographyexpert.PhotographyExpertEmbeddingService;
import com.epam.genai.service.photographyexpert.PhotographyExpertLlmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/photographyexpert")
@RequiredArgsConstructor
public class PhotographyExpertController {
  private final PhotographyExpertEmbeddingService photographyExpertEmbeddingService;
  private final PhotographyExpertLlmService photographyExpertLlmService;

  @PostMapping("init")
  public void photographyExpertInit() {
    Runnable runnable = photographyExpertEmbeddingService::loadDocuments;
    runnable.run();
  }

  @PostMapping(path = "query", consumes = "text/plain", produces = "text/plain")
  public String photographyExpert(@RequestBody String prompt) {
    return photographyExpertLlmService.query(prompt);
  }
}

package com.epam.genai.rest;

import com.epam.genai.rest.model.InputRequest;
import com.epam.genai.service.LensRecommendationLlmService;
import com.epam.genai.service.SimpleChatLlmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GenAiController {

  private final SimpleChatLlmService simpleChatLlmService;
  private final LensRecommendationLlmService lensRecommendationLlmService;

  @PostMapping(path = "chat", consumes = "application/json", produces = "text/plain")
  public ResponseEntity<String> chat(@RequestBody InputRequest request) {
    String input = request.getInput();
    String response = simpleChatLlmService.simpleChat(input, request.getTemperature(), request.getModel());

    return ResponseEntity.ok(response);
  }

  @PostMapping(path = "recommendLens", consumes = "application/json", produces = "text/plain")
  public ResponseEntity<String> recommendLens(@RequestBody InputRequest request) {
    String input = request.getInput();
    String response = lensRecommendationLlmService.recommendLenses(input, request.getTemperature());

    return ResponseEntity.ok(response);
  }
}

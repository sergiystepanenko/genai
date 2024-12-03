package com.epam.genai.rest;

import com.epam.genai.rest.model.InputRequest;
import com.epam.genai.service.OpenAiService;
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

  private final OpenAiService openAiService;

  @PostMapping(path = "chat", consumes = "application/json", produces = "text/plain")
  public ResponseEntity<String> chat(@RequestBody InputRequest request) {
    String input = request.getInput();
    String response = openAiService.simpleChat(input, request.getTemperature());

    return ResponseEntity.ok(response);
  }
}

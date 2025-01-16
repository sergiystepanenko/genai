package com.epam.genai.service.photographyexpert;

import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PhotographyExpertPlugin {
  private final PhotographyExpertEmbeddingService photographyExpertEmbeddingService;

  @DefineKernelFunction(
      name = "photography_query",
      description = """
          Provides expert knowledge or suggestion about photography topics such as
          Concepts & Terminology,
          Camera Equipment,
          Photo Editing & Post-Processing,
          Color Management & Printing,
          Photography Techniques & Styles""",
      returnDescription = "Returns expert knowledge or suggestion about asked topic.",
      returnType = "java.util.List")
  public List<String> query(
      @KernelFunctionParameter(name = "prompt", description = "The prompt from user about photography.")
      String prompt
  ) {
    log.debug("Function photography_query params: prompt={}", prompt);
    List<String> response = photographyExpertEmbeddingService.searchDocument(prompt);
    log.trace("Function photography_query returns: {}", response);
    return response;
  }
}

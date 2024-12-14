package com.epam.genai.service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmServiceBase {
  private final ApplicationContext applicationContext;
  private final OpenAIAsyncClient openAIAsyncClient;
  private final LlmClientProperties llmClientProperties;

  protected ChatHistory history;

  protected Kernel createKernal(KernelPlugin kernelPlugin) {
    return createKernal(null, kernelPlugin);
  }

  protected Kernel createKernal(final String model, KernelPlugin kernelPlugin) {
    String llmModel = model != null ? model : llmClientProperties.getModel();

    log.debug("Model: {}", llmModel);
    log.debug("KernelPlugin: {}", kernelPlugin != null ? kernelPlugin.getName() : "n/a");
    return applicationContext.getBean(Kernel.class, openAIAsyncClient, llmModel, kernelPlugin);
  }

  protected ChatCompletionService createChatCompletionService(Kernel kernel) {
    return applicationContext.getBean(ChatCompletionService.class, kernel);
  }

  protected void logResult(ChatMessageContent<?> result) {
    log.debug(result.getAuthorRole() + ":\n" + result.getContent());
  }
}

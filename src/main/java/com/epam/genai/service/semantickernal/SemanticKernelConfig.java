package com.epam.genai.service.semantickernal;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.KeyCredential;
import com.epam.genai.service.OpenAiClientProperties;
import com.epam.genai.service.semantickernal.model.BookFormat;
import com.google.gson.Gson;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.ServiceNotFoundException;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SemanticKernelConfig {
  private final OpenAiClientProperties openAiClientProperties;
  private final BookPlugin bookPlugin;

  @Bean
  public OpenAIAsyncClient openAIAsyncClient() {
    return new OpenAIClientBuilder()
        .credential(new KeyCredential(openAiClientProperties.getKey()))
        .endpoint(openAiClientProperties.getEndpoint())
        .buildAsyncClient();
  }

  @Bean
  public Kernel kernel(OpenAIAsyncClient openAIAsyncClient) {
    ChatCompletionService chatService = OpenAIChatCompletion.builder()
        .withModelId(openAiClientProperties.getModel())
        .withOpenAIAsyncClient(openAIAsyncClient)
        .build();

    ContextVariableTypes
        .addGlobalConverter(ContextVariableTypeConverter.builder(BookFormat.class)
            .fromObject(o -> Enum.valueOf(BookFormat.class, (String) o))
            .toPromptString(new Gson()::toJson)
            .build());

    KernelPlugin bookKernelPlugin = KernelPluginFactory.createFromObject(
        bookPlugin,
        "BookPlugin"
    );

    return Kernel.builder()
        .withAIService(ChatCompletionService.class, chatService)
        .withPlugin(bookKernelPlugin)
        .build();
  }

  @Bean
  public ChatCompletionService chatCompletionService(Kernel kernel) {
    try {
      return kernel.getService(ChatCompletionService.class);
    } catch (ServiceNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}

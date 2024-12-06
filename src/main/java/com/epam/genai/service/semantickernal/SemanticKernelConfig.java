package com.epam.genai.service.semantickernal;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.KeyCredential;
import com.epam.genai.service.LlmClientProperties;
import com.epam.genai.service.semantickernal.model.BookFormat;
import com.google.gson.Gson;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.services.ServiceNotFoundException;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@RequiredArgsConstructor
public class SemanticKernelConfig {
  private final LlmClientProperties llmClientProperties;

  @Bean
  public OpenAIAsyncClient openAIAsyncClient() {
    return new OpenAIClientBuilder()
        .credential(new KeyCredential(llmClientProperties.getKey()))
        .endpoint(llmClientProperties.getEndpoint())
        .buildAsyncClient();
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public Kernel kernel(OpenAIAsyncClient openAIAsyncClient, String model, KernelPlugin kernelPlugin) {
    ChatCompletionService chatService = OpenAIChatCompletion.builder()
        .withModelId(model)
        .withOpenAIAsyncClient(openAIAsyncClient)
        .build();

    ContextVariableTypes
        .addGlobalConverter(ContextVariableTypeConverter.builder(BookFormat.class)
            .fromObject(o -> Enum.valueOf(BookFormat.class, (String) o))
            .toPromptString(new Gson()::toJson)
            .build());

    Kernel.Builder kernalBuilder = Kernel.builder()
        .withAIService(ChatCompletionService.class, chatService);

    if (kernelPlugin != null) {
      kernalBuilder.withPlugin(kernelPlugin);
    }

    return kernalBuilder.build();
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ChatCompletionService chatCompletionService(Kernel kernel) {
    try {
      return kernel.getService(ChatCompletionService.class);
    } catch (ServiceNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}

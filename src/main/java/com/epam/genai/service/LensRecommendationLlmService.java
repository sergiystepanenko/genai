package com.epam.genai.service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.epam.genai.service.semantickernal.LensRecommendationPlugin;
import com.epam.genai.service.semantickernal.model.lens.Brand;
import com.epam.genai.service.semantickernal.model.lens.Lens;
import com.epam.genai.service.semantickernal.model.lens.LensMount;
import com.epam.genai.service.semantickernal.model.lens.PhotographyGenre;
import com.google.gson.Gson;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationContext.Builder;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LensRecommendationLlmService extends LlmServiceBase {
  private final LensRecommendationPlugin lensRecommendationPlugin;
  private KernelPlugin bookKernelPlugin;

  public LensRecommendationLlmService(ApplicationContext applicationContext, OpenAIAsyncClient openAIAsyncClient,
                                      LlmClientProperties llmClientProperties, LensRecommendationPlugin lensRecommendationPlugin) {
    super(applicationContext, openAIAsyncClient, llmClientProperties);
    this.lensRecommendationPlugin = lensRecommendationPlugin;
  }

  @PostConstruct
  public void init() {
    ContextVariableTypes
        .addGlobalConverter(ContextVariableTypeConverter.builder(Brand.class)
            .fromObject(o -> Enum.valueOf(Brand.class, (String) o))
            .toPromptString(new Gson()::toJson)
            .build());

    ContextVariableTypes
        .addGlobalConverter(ContextVariableTypeConverter.builder(LensMount.class)
            .fromObject(o -> Enum.valueOf(LensMount.class, (String) o))
            .toPromptString(new Gson()::toJson)
            .build());

    ContextVariableTypes
        .addGlobalConverter(ContextVariableTypeConverter.builder(PhotographyGenre.class)
            .fromObject(o -> Enum.valueOf(PhotographyGenre.class, (String) o))
            .toPromptString(new Gson()::toJson)
            .build());

    ContextVariableTypes
        .addGlobalConverter(ContextVariableTypeConverter.builder(Lens.class)
            .toPromptString(new Gson()::toJson)
            .build());

    bookKernelPlugin = KernelPluginFactory.createFromObject(
        lensRecommendationPlugin,
        "lensRecommendationPlugin"
    );
  }

  public String recommendLenses(String prompt, double temperature) {
    Kernel kernel = createKernal(bookKernelPlugin);
    ChatCompletionService chatCompletionService = createChatCompletionService(kernel);

    InvocationContext invocationContext = new Builder()
        .withReturnMode(InvocationReturnMode.FULL_HISTORY)
        .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
        .withPromptExecutionSettings(PromptExecutionSettings.builder()
            .withTemperature(temperature)
            .build())
        .build();

    // Create a history to store the conversation
    history = new ChatHistory("""
        You are a expert in photography gear, especially in camera lens.
        You can recommend photographers to select the best lens for their specific use case, taking into account their camera brand or model, lens mount, budget, and photography genre.""");
    history.addUserMessage(prompt);

    List<ChatMessageContent<?>> results =
        chatCompletionService.getChatMessageContentsAsync(history, kernel, invocationContext).block();

    log.debug("-------------------------chart history-------------------------");
    String assistantResponse = null;
    for (ChatMessageContent<?> result : Objects.requireNonNull(results)) {
      logResult(result);
      if (result.getAuthorRole() == AuthorRole.ASSISTANT && result.getContent() != null) {
        assistantResponse = result.getContent();
      }
    }

    return assistantResponse;
  }
}

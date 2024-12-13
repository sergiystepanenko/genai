package com.epam.genai.service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.epam.genai.rest.model.InputRequest;
import com.epam.genai.service.semantickernal.BookPlugin;
import com.microsoft.semantickernel.Kernel;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmService {
  private final ApplicationContext applicationContext;
  private final OpenAIAsyncClient openAIAsyncClient;
  private final LlmClientProperties llmClientProperties;

  private final BookPlugin bookPlugin;
  private ChatHistory history;
  private KernelPlugin bookKernelPlugin;

  @PostConstruct
  public void init() {
    bookKernelPlugin = KernelPluginFactory.createFromObject(
        bookPlugin,
        "BookPlugin"
    );
  }

  private Kernel createKernal(final String model, KernelPlugin kernelPlugin) {
    String llmModel = model != null ? model : llmClientProperties.getModel();

    log.debug("Model: {}", llmModel);
    log.debug("KernelPlugin: {}", kernelPlugin != null ? kernelPlugin.getName() : "n/a");
    return applicationContext.getBean(Kernel.class, openAIAsyncClient, llmModel, kernelPlugin);
  }

  private ChatCompletionService createChatCompletionService(Kernel kernel) {
    return applicationContext.getBean(ChatCompletionService.class, kernel);
  }

  public String simpleChat(String prompt, double temperature, String model) {
    Kernel kernel = createKernal(model, null);
    ChatCompletionService chatCompletionService = createChatCompletionService(kernel);

    // Enable planning
    InvocationContext invocationContext = new Builder()
        .withReturnMode(InvocationReturnMode.LAST_MESSAGE_ONLY)
        .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
        .withPromptExecutionSettings(PromptExecutionSettings.builder()
            .withTemperature(temperature)
            .build())
        .build();

    // Create a history to store the conversation
    ChatHistory history = new ChatHistory();
    history.addUserMessage(prompt);

    List<ChatMessageContent<?>> results =
        chatCompletionService.getChatMessageContentsAsync(history, kernel, invocationContext).block();

    StringBuilder stringBuilder = new StringBuilder();

    for (ChatMessageContent<?> result : Objects.requireNonNull(results)) {
      logResult(result);
      if (result.getAuthorRole() == AuthorRole.ASSISTANT && result.getContent() != null) {
        stringBuilder.append(result);
      }
      // Add the message from the agent to the chat history
      history.addMessage(result);
    }

    return stringBuilder.toString();
  }

  public String chatLibrarian(String prompt, String model) {
    return this.chatLibrarian(prompt, InputRequest.DEFAULT_TEMPERATURE, model);
  }

  public String chatLibrarian(String prompt, double temperature, String model) {
    Kernel kernel = createKernal(model, bookKernelPlugin);
    ChatCompletionService chatCompletionService = createChatCompletionService(kernel);

    InvocationContext invocationContext = new Builder()
        .withReturnMode(InvocationReturnMode.FULL_HISTORY)
        .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
        .withPromptExecutionSettings(PromptExecutionSettings.builder()
            .withTemperature(temperature)
            .build())
//        .withContextVariableConverter(ContextVariableTypeConverter.builder(BookFormat.class)
//            //.fromObject(o -> (BookFormat) o)
//            .toPromptString(new Gson()::toJson)
//            .build())
        .build();

    // Create a history to store the conversation
    history = (history == null) ? new ChatHistory("You are a librarian, expert about books.") : history;

    history.addUserMessage(prompt);

    List<ChatMessageContent<?>> results =
        chatCompletionService.getChatMessageContentsAsync(history, kernel, invocationContext).block();

    String assistantResponse = null;
    log.debug("-------------------------chart history-------------------------");
    for (ChatMessageContent<?> result : Objects.requireNonNull(results)) {
      logResult(result);
      if (result.getAuthorRole() == AuthorRole.ASSISTANT && result.getContent() != null) {
        assistantResponse = result.getContent();
      }
    }

    history = new ChatHistory(results);

    return assistantResponse;
  }

  private void logResult(ChatMessageContent<?> result) {
    log.debug(result.getAuthorRole() + ":\n" + result.getContent());
  }
}

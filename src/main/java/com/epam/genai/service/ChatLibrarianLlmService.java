package com.epam.genai.service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.epam.genai.rest.model.InputRequest;
import com.epam.genai.service.semantickernal.BookPlugin;
import com.epam.genai.service.semantickernal.model.book.BookFormat;
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
public class ChatLibrarianLlmService extends LlmServiceBase {
  private final BookPlugin bookPlugin;
  private KernelPlugin bookKernelPlugin;

  public ChatLibrarianLlmService(ApplicationContext applicationContext, OpenAIAsyncClient openAIAsyncClient,
                                 LlmClientProperties llmClientProperties, BookPlugin bookPlugin) {
    super(applicationContext, openAIAsyncClient, llmClientProperties);
    this.bookPlugin = bookPlugin;
  }

  @PostConstruct
  public void init() {
    ContextVariableTypes
        .addGlobalConverter(ContextVariableTypeConverter.builder(BookFormat.class)
            .fromObject(o -> Enum.valueOf(BookFormat.class, (String) o))
            .toPromptString(new Gson()::toJson)
            .build());

    bookKernelPlugin = KernelPluginFactory.createFromObject(
        bookPlugin,
        "BookPlugin"
    );
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
}

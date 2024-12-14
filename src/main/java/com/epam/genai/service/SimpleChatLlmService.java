package com.epam.genai.service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationContext.Builder;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import java.util.List;
import java.util.Objects;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class SimpleChatLlmService extends LlmServiceBase {

  public SimpleChatLlmService(ApplicationContext applicationContext,
                              OpenAIAsyncClient openAIAsyncClient, LlmClientProperties llmClientProperties) {
    super(applicationContext, openAIAsyncClient, llmClientProperties);
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
}

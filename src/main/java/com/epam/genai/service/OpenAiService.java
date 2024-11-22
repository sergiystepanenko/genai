package com.epam.genai.service;

import com.epam.genai.rest.model.InputRequest;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {
  private final ChatCompletionService chatCompletionService;
  private final Kernel kernel;

  private ChatHistory history;

  public String simpleChat(String prompt, double temperature) {

    // Enable planning
    InvocationContext invocationContext = new Builder()
        .withReturnMode(InvocationReturnMode.FULL_HISTORY)
        .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
        .withPromptExecutionSettings(PromptExecutionSettings.builder()
            .withTemperature(temperature)
            .build())
        .build();

    // Create a history to store the conversation
    ChatHistory history = new ChatHistory();
    history.addUserMessage(prompt);

    List<ChatMessageContent<?>> results =
        chatCompletionService.getChatMessageContentsAsync(history, this.kernel, invocationContext).block();

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

  public String chatLibrarian(String prompt) {
    return this.chatLibrarian(prompt, InputRequest.DEFAULT_TEMPERATURE);
  }

  public String chatLibrarian(String prompt, double temperature) {
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
    history = (history == null) ? new ChatHistory("""
        You are a librarian, expert about books.""") : history;

    history.addUserMessage(prompt);

    List<ChatMessageContent<?>> results =
        chatCompletionService.getChatMessageContentsAsync(history, this.kernel, invocationContext).block();

    String assistantResponse = null;
    log.debug("-------------------------chart history-------------------------");
    for (ChatMessageContent<?> result : Objects.requireNonNull(results)) {
      logResult(result);
      if (result.getAuthorRole() == AuthorRole.ASSISTANT && result.getContent() != null) {
        assistantResponse = result.getContent();
      }
      // Add the message from the agent to the chat history
//      history.addMessage(result);
    }

    history = new ChatHistory(results);

    return assistantResponse;
  }

  private void logResult(ChatMessageContent<?> result) {
    log.debug(result.getAuthorRole() + ":\n" + result.getContent());
  }
}

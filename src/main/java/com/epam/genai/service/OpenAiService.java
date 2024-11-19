package com.epam.genai.service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.KeyCredential;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationContext.Builder;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.services.ServiceNotFoundException;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {

  private final OpenAiClientProperties openAiClientProperties;

  private ChatCompletionService chatCompletionService;
  private InvocationContext invocationContext;
  private Kernel kernel;

  @PostConstruct
  private void init() {
    OpenAIAsyncClient client = createOpenAIClient();
    // Create the chat completion service
    ChatCompletionService chatService = OpenAIChatCompletion.builder()
        .withModelId(openAiClientProperties.getModel())
        .withOpenAIAsyncClient(client)
        .build();

    // Initialize the kernel
    this.kernel = Kernel.builder()
        .withAIService(ChatCompletionService.class, chatService)
        .build();

    // Enable planning
    this.invocationContext = new Builder()
        .withReturnMode(InvocationReturnMode.LAST_MESSAGE_ONLY)
        .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
//        .withContextVariableConverter(ContextVariableTypeConverter.builder(LightModel.class)
//            .toPromptString(new Gson()::toJson)
//            .build())
        .build();


    try {
      this.chatCompletionService = kernel.getService(ChatCompletionService.class);
    } catch (ServiceNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @PostConstruct
  private OpenAIAsyncClient createOpenAIClient() {
    return new OpenAIClientBuilder()
        .credential(new KeyCredential(openAiClientProperties.getKey()))
        .endpoint(openAiClientProperties.getEndpoint())
        .buildAsyncClient();
  }

  public String simpleChat(String prompt) {
    // Create a history to store the conversation
    ChatHistory history = new ChatHistory();
    history.addUserMessage(prompt);

    List<ChatMessageContent<?>> results =
        chatCompletionService.getChatMessageContentsAsync(history, this.kernel, this.invocationContext).block();

    StringBuilder stringBuilder = new StringBuilder();

    for (ChatMessageContent<?> result : Objects.requireNonNull(results)) {
      if (result.getAuthorRole() == AuthorRole.ASSISTANT && result.getContent() != null) {
        log.debug("Chat message result:\n" + result.getContent());
        stringBuilder.append(result);
      }
      // Add the message from the agent to the chat history
      history.addMessage(result);
    }

    return stringBuilder.toString();
  }
}

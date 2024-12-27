package com.epam.genai.service.photographyexpert;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.epam.genai.service.LlmClientProperties;
import com.epam.genai.service.LlmServiceBase;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PhotographyExpertLlmService extends LlmServiceBase {
  private final PhotographyExpertPlugin photographyExpertPlugin;
  private KernelPlugin kernelPlugin;

  public PhotographyExpertLlmService(ApplicationContext applicationContext, OpenAIAsyncClient openAIAsyncClient,
                                     LlmClientProperties llmClientProperties, PhotographyExpertPlugin photographyExpertPlugin) {
    super(applicationContext, openAIAsyncClient, llmClientProperties);
    this.photographyExpertPlugin = photographyExpertPlugin;
  }

  @PostConstruct
  public void init() {

    kernelPlugin = KernelPluginFactory.createFromObject(
        photographyExpertPlugin,
        "photographyExpertPlugin"
    );
  }

  public String query(String prompt) {
    Kernel kernel = createKernal(kernelPlugin);
    ChatCompletionService chatCompletionService = createChatCompletionService(kernel);

    InvocationContext invocationContext = new Builder()
        .withReturnMode(InvocationReturnMode.FULL_HISTORY)
        .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
        .withPromptExecutionSettings(PromptExecutionSettings.builder()
            .withTemperature(0.5)
            .build())
        .build();

    // Create a history to store the conversation
    history = new ChatHistory();

    history.addSystemMessage("""        
        You are a helpful photography assistant and an expert in photography topics like
        Concepts & Terminology,
        Camera Equipment,
        Photo Editing & Post-Processing,
        Color Management & Printing,
        Photography Techniques & Styles and others.
        
            Use only the information returned by photography_query function to answer the question.
            Do not use any other information. If you do not know, simply answer: "I don't know".
        """
    );
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

package com.epam.genai.service.embedding;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.epam.genai.service.LlmClientProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

  private final MilvusRepository milvusRepository;
  private final OpenAIAsyncClient openAIAsyncClient;
  private final LlmClientProperties llmClientProperties;

  public void saveEmbedding(String text) {
    Embeddings embeddings = getEmbeddingsFromString(text);
    if (embeddings != null) {
      milvusRepository.save(EmbeddingUtils.embeddingsToFloatList(embeddings), text);
    }
  }

  public List<String> searchEmbedding(String text, int topLimit) {
    Embeddings embeddings = getEmbeddingsFromString(text);
    return milvusRepository.search(EmbeddingUtils.embeddingsToFloatList(embeddings), topLimit);
  }

  private Embeddings getEmbeddingsFromString(String text) {
    EmbeddingsOptions qembeddingsOptions = new EmbeddingsOptions(List.of(text));
    return openAIAsyncClient.getEmbeddings(llmClientProperties.getEmbeddingModel(), qembeddingsOptions).block();
  }
}
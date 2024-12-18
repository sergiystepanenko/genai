package com.epam.genai.service.embedding;

import com.azure.ai.openai.models.Embeddings;
import java.util.List;
import lombok.NonNull;

public final class EmbeddingUtils {
  private EmbeddingUtils() {
  }

  public static List<Float> embeddingsToFloatList(@NonNull Embeddings embeddings) {
    return embeddings.getData().stream()
        .flatMap(data -> data.getEmbedding().stream())
        .toList();
  }
}

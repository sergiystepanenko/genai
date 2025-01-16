package com.epam.genai.service.photographyexpert;

import com.epam.genai.service.embedding.MilvusProperties;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.document.transformer.jsoup.HtmlToTextDocumentTransformer;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.param.IndexType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PhotographyExpertEmbeddingService {
  private static final int DIMENSIONS = 384;
  private static final int MAX_RESULTS = 3;
  private static final String COLLECTION_NAME = "photo_tutorials";
  private static final String DOC_FOLDER_NAME = "photo-tutorials";
  private final MilvusProperties milvusProperties;
  // use a local in-process embedding model
  private final EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel();

  public void loadDocuments() {
    long startTime = System.nanoTime();
    EmbeddingStore<TextSegment> embeddingStore = getEmbeddingStore();
    // clear a collection on new run
    embeddingStore.removeAll();

    List<String> files = getAllFileNames(DOC_FOLDER_NAME);
    files.forEach(fileName -> {
      log.info("Loading embedding from {}", fileName);
      loadDocument(fileName, embeddingModel, embeddingStore);
    });

    // End time
    long endTime = System.nanoTime();

    // Calculate execution time in milliseconds
    long duration = (endTime - startTime) / 1_000_000;
    log.info("Document init time: {} ms", duration);
  }

  public List<String> searchDocument(String query) {
    EmbeddingStore<TextSegment> embeddingStore = getEmbeddingStore();

    Embedding queryEmbedding = embeddingModel.embed(query).content();
    EmbeddingSearchResult<TextSegment> embeddingSearchResult = embeddingStore.search(EmbeddingSearchRequest.builder()
        .queryEmbedding(queryEmbedding)
        .maxResults(MAX_RESULTS)
        .build());

    if (embeddingSearchResult.matches().isEmpty()) {
      log.warn("No embedding match found for query {}", query);
      return List.of();
    }

    log.debug("Found embedding matches:\n{}", embeddingSearchResult.matches());

    return embeddingSearchResult.matches().stream()
        .map(match -> match.embedded().text())
        .toList();
  }

  @SneakyThrows
  private List<String> getAllFileNames(String folderName) {
    Path folderPath = Paths.get(ClassLoader.getSystemResource(folderName).toURI());

    // List all files in the folder and collect their names
    return Files.list(folderPath)
        .filter(Files::isRegularFile) // Only regular files
        .map(Path::toString)     // Extract file names
        .toList();
  }

  private EmbeddingStore<TextSegment> getEmbeddingStore() {
    return MilvusEmbeddingStore.builder()
        .uri("http://" + milvusProperties.getHost() + ":" + milvusProperties.getPort())
        .collectionName(COLLECTION_NAME)
        .indexType(IndexType.AUTOINDEX)
        .dimension(DIMENSIONS)
        .build();
  }

  private void loadDocument(String fileName, EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
    DocumentParser documentParser = new TextDocumentParser();
    Document documentHtml = FileSystemDocumentLoader.loadDocument(fileName, documentParser);

    // Clean up html
    HtmlToTextDocumentTransformer extractor = new HtmlToTextDocumentTransformer();
    Document document = extractor.transform(documentHtml);
    document.metadata().remove(Document.ABSOLUTE_DIRECTORY_PATH);

    // Split this document into segments
    // DocumentSplitter splitter = DocumentSplitters.recursive(500, 0);
    DocumentBySentenceSplitter splitter = new DocumentBySentenceSplitter(500, 50);
    List<TextSegment> segments = splitter.split(document);

    List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

    embeddingStore.addAll(embeddings, segments);
  }
}

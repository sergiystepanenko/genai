package com.epam.genai.service.embedding;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MilvusRepository {
  private static final String COLLECTION_NAME = "embeddings";
  private static final String ID_FIELD = "id";
  private static final String VECTOR_FIELD = "vector";
  private static final String TEXT_FIELD = "text";
  private static final Integer VECTOR_DIMENSIONS = 1536;
  private final MilvusProperties milvusProperties;
  private MilvusClientV2 milvusClient;

  private boolean initialized = false;

  public void init() {
    milvusClient = createMilvusClient();

    // Drop collection if exists
    milvusClient.dropCollection(DropCollectionReq.builder()
        .collectionName(COLLECTION_NAME)
        .build());

    // Quickly create a collection with "id" field and "vector" field
    milvusClient.createCollection(CreateCollectionReq.builder()
        .collectionName(COLLECTION_NAME)
        .dimension(VECTOR_DIMENSIONS)
        .collectionSchema(CreateCollectionReq.CollectionSchema.builder()
            .fieldSchemaList(createFieldSchemas())
            .build())
        .indexParams(List.of(IndexParam.builder()
            .fieldName(VECTOR_FIELD)
            .indexName(VECTOR_FIELD + "_idx")
            .metricType(IndexParam.MetricType.COSINE)
            .indexType(IndexParam.IndexType.AUTOINDEX)
            .build()))
        .build());

    // All search and query operations within Milvus are executed in memory.
    // Load the collection to memory before conducting a vector similarity search.
    milvusClient.loadCollection(
        LoadCollectionReq.builder()
            .collectionName(COLLECTION_NAME)
            .build());

    initialized = true;
  }

  private MilvusClientV2 createMilvusClient() {
    ConnectConfig connectConfig = ConnectConfig.builder()
        .uri("http://" + milvusProperties.getHost() + ":" + milvusProperties.getPort())
        .build();

    return new MilvusClientV2(connectConfig);
  }

  public void save(List<Float> vector, String text) {
    InsertResp insertResp = getMilvusClient().insert(InsertReq.builder()
        .collectionName(COLLECTION_NAME)
        .data(buildData(vector, text))
        .build());

    log.debug("Inserted PK:{}", insertResp.getPrimaryKeys());
  }

  public List<String> search(List<Float> vector, int topLimit) {
    SearchResp searchResp = getMilvusClient().search(SearchReq.builder()
        .collectionName(COLLECTION_NAME)
        .topK(topLimit)
        .data(Collections.singletonList(new FloatVec(vector)))
        .annsField(VECTOR_FIELD)
        .consistencyLevel(ConsistencyLevel.EVENTUALLY)
        .outputFields(List.of(TEXT_FIELD, ID_FIELD))
        .build());

    List<String> results = new ArrayList<>();

    List<List<SearchResp.SearchResult>> respSearchResults = searchResp.getSearchResults();
    for (List<SearchResp.SearchResult> searchResults : respSearchResults) {
      for (SearchResp.SearchResult searchResult : searchResults) {
        String text = searchResult.getEntity().get(TEXT_FIELD).toString();
        log.debug("Score: {}, ID: {}, Text: {}", searchResult.getScore(), searchResult.getId(), text);
        results.add(text);
      }
    }

    return results;
  }

  private MilvusClientV2 getMilvusClient() {
    if (!initialized) {
      init();
    }
    return milvusClient;
  }

  private static List<CreateCollectionReq.FieldSchema> createFieldSchemas() {
    return List.of(
        createFieldSchema(ID_FIELD, "Primary key", DataType.Int64, true, null),
        createFieldSchema(TEXT_FIELD, "Text", DataType.VarChar, false, null),
        createFieldSchema(VECTOR_FIELD, "Vector", DataType.FloatVector, false, VECTOR_DIMENSIONS)
    );
  }

  private static CreateCollectionReq.FieldSchema createFieldSchema(String name, String desc, DataType dataType, boolean isPrimary,
                                                                   Integer dimension) {
    CreateCollectionReq.FieldSchema fieldSchema = CreateCollectionReq.FieldSchema.builder()
        .name(name)
        .description(desc)
        .autoID(isPrimary)
        .isPrimaryKey(isPrimary)
        .dataType(dataType)
        .build();
    if (null != dimension) {
      fieldSchema.setDimension(dimension);
    }
    return fieldSchema;
  }

  private static List<JsonObject> buildData(List<Float> vector, String text) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add(VECTOR_FIELD, gson.toJsonTree(vector));
    jsonObject.addProperty(TEXT_FIELD, text);

    return List.of(jsonObject);
  }
}

# GenAI Practical Application Development using Java

## The technological stack

- Spring Boot
- Java 21
- Microsoft Semantic Kernel
- langchain4j
- EPAM DIAL api

Use EPAM VPN to access EPAM DIAL api.
ChatGPT request is proxied via EPAM DIAL, gpt-4o model is used by default.

## REST API

To simple response from ChatGPT model use REST route:

    `POST http://localhost:8080/api/chat
    Content-Type: application/json
    { 
        "input": "I want to find top-10 books about world history",
        "model": "Mistral-7B-Instruct"
        "temperature": 0.9
    }`

_model_ and _temperature_ params are optional.

LlmServiceIT integration test is used for chart history with plugins.

Models Mistral-7B-Instruct, Llama-3-8B-Instruct do not support plugins usages via EPAM DIAL. These model as hosted in
EPAM env

    `com.azure.core.exception.HttpResponseException: Status code 400, "{"object":"error","message":"\"auto\" tool choice requires --enable-auto-tool-choice and --tool-call-parser to be set","type":"BadRequestError","param":null,"code":400}"`

## Embedding

Milvus is used as a vector db. Use this [docker compose](docker/milvus-standalone-docker-compose.yml) to run Milvus
locally.
Embedding model is configured in [application.yaml](src/main/resources/application.yaml) _llm-client.embedding-model_
property.

Insert data into vector db:

    `POST http://localhost:8080/api/embedding
    Content-Type: application/json
    {
    "input": "embedding text"
    }`

Similarity search:

    `POST http://localhost:8080/api/embedding/search
    Content-Type: application/json
    {
    "input": "Who shoots canon?"
    }`

## RAG Service

RAG Service is photography assistant and an expert in photography topics like:

* Concepts & Terminology
* Camera Equipment
* Photo Editing & Post-Processing
* Color Management & Printing
* Photography Techniques & Styles and others

Vector db is initialized by processing html pages with the photography articles

    `POST http://localhost:8080/photographyexpert/init

Query photography expert:

    `POST http://localhost:8080/photographyexpert/query
     Content-Type: text/plain

     Recommend how to use telephoto lens in landscape photography?

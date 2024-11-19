# GenAI Practical Application Development using Java

The tech stack:

- Spring Boot
- Java 21
- Microsoft Semantic Kernel
- EPAM DIAL api

Use EPAM VPN to access EPAM DIAL api

## Task 1: Generative AI basics

To simple response from ChatGPT model use REST route:

    POST http://localhost:8080/api/chat
    Content-Type: application/json
    { "input": "I want to find top-10 books about world history" }

ChatGPT request is proxied via EPAM DIAL, gpt-4o model is used.
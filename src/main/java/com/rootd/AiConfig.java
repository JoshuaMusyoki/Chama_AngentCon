package com.rootd;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

@Configuration
public class AiConfig {

    @Bean
    public ChamaArbitratorAgent chamaArbitratorAgent() throws IOException {
        System.out.println("[AI CONFIG] Initializing Mpatanishi AI...");

        // 1. Setup Models
        // Use environment variable for API Key in production
        String apiKey = System.getenv("GOOGLE_AI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "YOUR_GEMINI_API_KEY"; // Fallback for local
        }

        GoogleAiGeminiChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-3-flash-preview")
                .temperature(0.2)
                .timeout(Duration.ofSeconds(120))
                .logRequestsAndResponses(true)
                .build();

        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        // 2. Ingest Bylaws (Safe for JAR execution)
        System.out.println("[AI CONFIG] Loading bylaws.txt...");
        ClassPathResource resource = new ClassPathResource("bylaws.txt");
        Path tempFile = Files.createTempFile("bylaws", ".txt");
        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        
        Document document = FileSystemDocumentLoader.loadDocument(tempFile, new ApacheTikaDocumentParser());
        
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(dev.langchain4j.data.document.splitter.DocumentSplitters.recursive(300, 0))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(document);

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .build();

        System.out.println("[AI CONFIG] Successfully loaded bylaws into vector store.");

        // 3. Build the Agent
        return AiServices.builder(ChamaArbitratorAgent.class)
                .chatLanguageModel(model)
                .tools(new MpesaTransactionTool())
                .contentRetriever(contentRetriever)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }
}

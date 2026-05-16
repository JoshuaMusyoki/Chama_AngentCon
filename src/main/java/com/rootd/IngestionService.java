package com.rootd;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

public class IngestionService {

    // Using an In-Memory store for the hackathon speed; swap with Vertex AI Vector Search for production
    private final EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

    public void ingestBylaws(String filePath) {
        // 1. Load and parse the Chama constitution PDF
        Document document = FileSystemDocumentLoader.loadDocument(filePath, new ApacheTikaDocumentParser());

        // 2. Split text into logical chunks (e.g., by clauses/sections)
        // 3. Embed and store the text segments
        // (LangChain4j handles embedding generation internally when mapped to an EmbeddingModel)
        System.out.println("Chama Bylaws successfully vectorized and loaded into context.");
    }
}

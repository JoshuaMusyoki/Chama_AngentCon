package com.rootd;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class Main {

    public static void main(String[] args) {
        System.out.println("====================================================");
        System.out.println("Initializing Mpatanishi AI Arbitration Engine...");
        System.out.println("====================================================");

        // 1. Setup Models
        String apiKey = "AIzaSyDjors_Ko23hXowxtdGmuz9K8u8sMuAOyE";
        GoogleAiGeminiChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-1.5-flash")
                .temperature(0.2)
                .timeout(Duration.ofSeconds(60))
                .build();

        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        // 2. Ingest Bylaws (RAG)
        System.out.println("[INFO] Loading Chama Bylaws...");
        Path resourceDirectory = Paths.get("src", "main", "resources", "bylaws.txt");
        Document document = FileSystemDocumentLoader.loadDocument(resourceDirectory, new ApacheTikaDocumentParser());
        
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(dev.langchain4j.data.document.splitter.DocumentSplitters.recursive(300, 0))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(document);

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2) // Look up top 2 relevant clauses
                .minScore(0.5)
                .build();

        System.out.println("[SUCCESS] Bylaws loaded into vector memory.");

        // 3. Build the Agent
        ChamaArbitratorAgent arbitrator = AiServices.builder(ChamaArbitratorAgent.class)
                .chatLanguageModel(model)
                .tools(new MpesaTransactionTool())
                .contentRetriever(contentRetriever) // Link RAG here
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        System.out.println("[SUCCESS] Mpatanishi Agent Architecture fully loaded.");
        System.out.println("----------------------------------------------------\n");

        // 4. Test Scenario
        String shengDispute = "Kamau alituma mchango tarehe 12 badala ya tarehe 5. " +
                "Anadai mtoi alikuwa mteja msupu kwa hosi, lakini treasurer anataka kumchapia fine ya mbao. " +
                "Bylaws zinasemaje kuhusu hii story?";

        System.out.println("Incoming Dispute Session Context (Sheng):");
        System.out.println("> \"" + shengDispute + "\"\n");
        System.out.println("Executing system arbitration and tool routing...");

        try {
            String verdict = arbitrator.mediateDispute(shengDispute);
            System.out.println("\n================== MPATANISHI AI VERDICT ==================");
            System.out.println(verdict);
            System.out.println("===========================================================");
        } catch (Exception e) {
            System.err.println("\n[ERROR] Failed to mediate dispute.");
            e.printStackTrace();
        }
    }
}
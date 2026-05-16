package com.rootd;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;

public class ArbitrationEngine {

    public static void main(String[] args) {
        // 1. Initialize Gemini 1.5 Pro via Google AI (using API Key)
        // Get your API key from: https://aistudio.google.com/app/apikey
        String apiKey = "YOUR_API_KEY_HERE"; 

        if ("AIzaSyA3ihXehOrDLJrk0SMlPe1LdfmYEGwTAbM".equals(apiKey)) {
            System.err.println("[ERROR] Please replace 'YOUR_API_KEY_HERE' in ArbitrationEngine.java");
            return;
        }

        GoogleAiGeminiChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-1.5-pro-latest")
                .temperature(0.2)
                .build();

        // 2. Build the Agent with Tools and RAG components
        ChamaArbitratorAgent arbitrator = AiServices.builder(ChamaArbitratorAgent.class)
                .chatLanguageModel(model)
                .tools(new MpesaTransactionTool()) 
                .build();

        // 3. Test Scenario
        String shengDispute = "Kamau alituma mchango tarehe 12 badala ya tarehe 5. " +
                "Anadai mtoi alikuwa mteja msupu kwa hosi, lakini treasurer anataka kumchapia fine ya mbao. " +
                "Bylaws zinasemaje kuhusu hii story?";

        String verdict = arbitrator.mediateDispute(shengDispute);

        System.out.println("--- Mpatanishi Verdict ---");
        System.out.println(verdict);
    }
}
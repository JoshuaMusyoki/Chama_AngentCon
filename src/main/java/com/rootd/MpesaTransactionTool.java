package com.rootd;

import dev.langchain4j.agent.tool.Tool;
import java.util.List;

public class MpesaTransactionTool {

    @Tool("Look up the transaction history, statement records, and contribution dates for a specific member")
    public String verifyMemberContributions(String memberName) {
        // Mocking a quick database/CSV lookup for the hackathon
        if (memberName.equalsIgnoreCase("Kamau")) {
            return "Transaction ID: QRE459FXX. Date: 12th May 2026. Amount: KES 5,000. Status: Received. " +
                    "Note: Late contribution (Deadline was 5th May).";
        }
        return "No transactions found for member: " + memberName;
    }
}

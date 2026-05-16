package com.rootd;

import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
public class ArbitrationController {

    private final ChamaArbitratorAgent arbitrator;

    public ArbitrationController(ChamaArbitratorAgent arbitrator) {
        this.arbitrator = arbitrator;
    }

    @PostMapping("/api/arbitrate")
    public ArbitrationResponse mediate(@RequestBody ArbitrationRequest request) {
        try {
            System.out.println("Received dispute: " + request.getDispute());
            String verdict = arbitrator.mediateDispute(request.getDispute());
            return new ArbitrationResponse(verdict);
        } catch (Exception e) {
            String errorMsg = "Error: ";
            if (e.getMessage().contains("UnknownHostException")) {
                errorMsg += "No internet connection or DNS issue. Please check your network.";
            } else if (e.getMessage().contains("timeout")) {
                errorMsg += "The AI took too long to respond. Please try again.";
            } else {
                errorMsg += "An unexpected error occurred: " + e.getMessage();
            }
            return new ArbitrationResponse(errorMsg);
        }
    }

    @GetMapping("/api/status")
    public String status() {
        return "Mpatanishi AI is Online";
    }

    public static class ArbitrationRequest {
        private String dispute;
        public String getDispute() { return dispute; }
        public void setDispute(String dispute) { this.dispute = dispute; }
    }

    public static class ArbitrationResponse {
        private String verdict;
        public ArbitrationResponse(String verdict) { this.verdict = verdict; }
        public String getVerdict() { return verdict; }
    }
}
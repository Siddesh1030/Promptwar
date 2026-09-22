package com.hackathon.phishing.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GeminiService {

    private final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    public String analyzeOffer(String offerText, String domainAgeInfo, String fileBase64, String fileName) throws Exception {
        String apiKey = System.getProperty("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getenv("GEMINI_API_KEY");
        }
        
        // HACKATHON DEMO FALLBACK: If no valid API key is provided, return a realistic mock response instead of crashing
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your_gemini_api_key_here")) {
            System.out.println("Using Mock AI Response (No valid API key found)");
            
            // Generate a dynamic mock response based on the text length for variety
            int mockThreat = offerText.toLowerCase().contains("check") || offerText.toLowerCase().contains("pay") || (fileBase64 != null) ? 85 : 20;
            String mockReasoning = mockThreat > 50 ? "The offer contains suspicious requests for advance payment or check cashing." : "The offer appears standard with no immediate financial demands.";
            String mockRedFlags = mockThreat > 50 ? "[\"Advance fee requested\", \"Check deposit scheme\"]" : "[]";
            String mockSuggestions = mockThreat > 50 ? "[\"Do not send any money or deposit any checks.\", \"Verify the recruiter's identity through the official company LinkedIn page.\", \"Report this communication to the platform where you received it.\"]" : "[\"Proceed with normal interview steps.\", \"Always verify employment contracts independently.\"]";
            
            return "{\"scam_threat_index\": " + mockThreat + ", \"reasoning\": \"" + mockReasoning + "\", \"red_flags\": " + mockRedFlags + ", \"suggestions\": " + mockSuggestions + "}";
        }

        String prompt = "You are an expert cybersecurity analyzer. " +
                "Please analyze the following data for potential phishing or scam indicators. " +
                "If only a URL is provided with no accompanying text (Offer Text is 'Not Applicable'), analyze the URL structure for common phishing patterns (e.g., typosquatting, strange TLDs) and weigh the Whois domain age heavily to calculate the Scam Threat Index. " +
                "If only text is provided (Domain Info is 'Not Applicable'), analyze the text for payment red flags and ignore domain age. " +
                "If an image or document is provided, analyze its content for phishing or scam indicators. " +
                "If both are provided, analyze both. " +
                "You must return the result as a valid JSON payload with exactly four fields: " +
                "1) 'scam_threat_index' (an integer from 0 to 100 representing the threat level), " +
                "2) 'red_flags' (an array of strings detailing the specific payment demand or scam indicators found), " +
                "3) 'reasoning' (a brief string explaining exactly why this is high risk or safe), " +
                "4) 'suggestions' (an array of strings giving the user 3 specific, actionable recommendations on what to do next based on these findings). " +
                "Do not include any markdown formatting (like ```json), just return the raw JSON object.\n\n" +
                "Domain Info: " + domainAgeInfo + "\n\n" +
                "Offer Text: " + offerText;

        ObjectMapper mapper = new ObjectMapper();
        var rootNode = mapper.createObjectNode();
        
        var contentsArray = mapper.createArrayNode();
        var contentObj = mapper.createObjectNode();
        var partsArray = mapper.createArrayNode();
        
        if (fileBase64 != null && !fileBase64.isEmpty()) {
            var filePart = mapper.createObjectNode();
            var inlineData = mapper.createObjectNode();
            
            String mimeType = "application/pdf";
            if (fileName != null) {
                String lowerName = fileName.toLowerCase();
                if (lowerName.endsWith(".png")) mimeType = "image/png";
                else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) mimeType = "image/jpeg";
            }
            
            // Clean base64 string if it contains data URI prefix
            String base64Data = fileBase64;
            if (base64Data.contains(",")) {
                base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
            }
            
            inlineData.put("mimeType", mimeType);
            inlineData.put("data", base64Data);
            filePart.set("inlineData", inlineData);
            partsArray.add(filePart);
        }

        var textPart = mapper.createObjectNode();
        textPart.put("text", prompt);
        partsArray.add(textPart);
        
        contentObj.set("parts", partsArray);
        contentsArray.add(contentObj);
        
        rootNode.set("contents", contentsArray);
        
        var configObj = mapper.createObjectNode();
        configObj.put("responseMimeType", "application/json");
        rootNode.set("generationConfig", configObj);
        
        String requestBody = mapper.writeValueAsString(rootNode);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to call Gemini API: " + response.body());
        }

        return parseWithJackson(response.body());
    }

    private String parseWithJackson(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        return root.path("candidates")
                   .get(0)
                   .path("content")
                   .path("parts")
                   .get(0)
                   .path("text")
                   .asText();
    }
}

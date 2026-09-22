package com.hackathon.phishing.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.phishing.dto.ScanRequest;
import com.hackathon.phishing.model.ScanRecord;
import com.hackathon.phishing.repository.ScanRecordRepository;
import com.hackathon.phishing.service.GeminiService;
import com.hackathon.phishing.service.WhoisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scan")
public class ScanController {

    private final WhoisService whoisService;
    private final GeminiService geminiService;
    private final ScanRecordRepository scanRecordRepository;
    private final ObjectMapper objectMapper;

    public ScanController(WhoisService whoisService, GeminiService geminiService, ScanRecordRepository scanRecordRepository) {
        this.whoisService = whoisService;
        this.geminiService = geminiService;
        this.scanRecordRepository = scanRecordRepository;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping
    public ResponseEntity<String> scanOffer(@RequestBody ScanRequest request) {
        boolean hasUrl = request.getCompanyUrl() != null && !request.getCompanyUrl().trim().isEmpty();
        boolean hasText = request.getOfferText() != null && !request.getOfferText().trim().isEmpty();
        boolean hasFile = request.getFileBase64() != null && !request.getFileBase64().trim().isEmpty();

        if (!hasUrl && !hasText && !hasFile) {
            return ResponseEntity.badRequest()
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"Please provide either a Company URL, Offer Letter Text, a File, or a combination.\"}");
        }

        try {
            String domainInfo = hasUrl ? whoisService.getDomainAgeInfo(request.getCompanyUrl()) : "Not Applicable";
            String safeOfferText = hasText ? request.getOfferText() : "Not Applicable";
            String fileData = hasFile ? request.getFileBase64() : null;
            String fileName = request.getFileName();
            String jsonResult = geminiService.analyzeOffer(safeOfferText, domainInfo, fileData, fileName);

            // Parse jsonResult to extract data for database storage
            JsonNode root = objectMapper.readTree(jsonResult);
            int threatIndex = root.path("scam_threat_index").asInt();
            String reasoning = root.path("reasoning").asText();
            String redFlagsStr = root.path("red_flags").toString(); // Store array as JSON string

            ScanRecord record = new ScanRecord();
            record.setCompanyUrl(request.getCompanyUrl());
            record.setOfferText(request.getOfferText());
            record.setScamThreatIndex(threatIndex);
            record.setReasoning(reasoning);
            record.setRedFlags(redFlagsStr);

            scanRecordRepository.save(record);

            return ResponseEntity.ok().header("Content-Type", "application/json").body(jsonResult);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                var errorNode = mapper.createObjectNode();
                errorNode.put("error", e.getMessage());
                String errorJson = mapper.writeValueAsString(errorNode);
                return ResponseEntity.internalServerError()
                        .header("Content-Type", "application/json")
                        .body(errorJson);
            } catch (Exception ex) {
                return ResponseEntity.internalServerError().body("{\"error\": \"Internal Server Error\"}");
            }
        }
    }
}

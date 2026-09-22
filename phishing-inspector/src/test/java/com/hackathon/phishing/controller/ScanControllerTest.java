package com.hackathon.phishing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.phishing.dto.ScanRequest;
import com.hackathon.phishing.repository.ScanRecordRepository;
import com.hackathon.phishing.service.GeminiService;
import com.hackathon.phishing.service.WhoisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(ScanController.class)
public class ScanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WhoisService whoisService;

    @MockBean
    private GeminiService geminiService;
    
    @MockBean
    private ScanRecordRepository scanRecordRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testScanOfferReturns200() throws Exception {
        ScanRequest request = new ScanRequest();
        request.setOfferText("You have been selected for a job. Please pay $500 for equipment.");
        request.setCompanyUrl("http://scam-company-example.com");

        String fakeResponse = "{\"scam_threat_index\": 95, \"red_flags\": [\"Payment requested\"], \"reasoning\": \"Asking for money upfront\"}";

        when(whoisService.getDomainAgeInfo(any())).thenReturn("Domain created 1 day ago");
        when(geminiService.analyzeOffer(any(), any(), any(), any())).thenReturn(fakeResponse);

        mockMvc.perform(post("/api/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(fakeResponse));
    }
}


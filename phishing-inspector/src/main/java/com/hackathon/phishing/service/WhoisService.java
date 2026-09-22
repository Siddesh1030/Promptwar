package com.hackathon.phishing.service;

import org.apache.commons.net.whois.WhoisClient;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WhoisService {

    public String getDomainAgeInfo(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "No URL provided";
        }
        
        try {
            String domain = extractDomain(url);
            if (domain == null) return "Invalid URL format";
            
            WhoisClient whois = new WhoisClient();
            whois.setConnectTimeout(5000);
            whois.setDefaultTimeout(5000);
            whois.connect(WhoisClient.DEFAULT_HOST);
            String whoisData = whois.query(domain);
            whois.disconnect();
            
            // Extract Creation Date (simple regex, whois formats vary wildly but this is a heuristic)
            Pattern pattern = Pattern.compile("(?i)(Creation Date|Created On|Registration Date)\\s*:?\\s*(.+)");
            Matcher matcher = pattern.matcher(whoisData);
            if (matcher.find()) {
                return "Domain created on: " + matcher.group(2).trim();
            }
            return "Domain age info not clearly found in WHOIS data. Could be a very new or obscure domain.";
            
        } catch (Exception e) {
            return "Error retrieving domain age: " + e.getMessage();
        }
    }

    private String extractDomain(String url) throws URISyntaxException {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        URI uri = new URI(url);
        String domain = uri.getHost();
        if (domain != null) {
            domain = domain.startsWith("www.") ? domain.substring(4) : domain;
        }
        return domain;
    }
}

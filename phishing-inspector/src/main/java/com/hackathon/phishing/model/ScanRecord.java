package com.hackathon.phishing.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scan_records")
public class ScanRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String companyUrl;

    @Column(columnDefinition = "TEXT")
    private String offerText;

    @Column(nullable = false)
    private int scamThreatIndex;

    @Column(columnDefinition = "TEXT")
    private String reasoning;

    @Column(columnDefinition = "TEXT")
    private String redFlags; // We'll store the JSON array as a string for simplicity

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyUrl() {
        return companyUrl;
    }
    public void setCompanyUrl(String companyUrl) {
        this.companyUrl = companyUrl;
    }

    public String getOfferText() {
        return offerText;
    }
    public void setOfferText(String offerText) {
        this.offerText = offerText;
    }

    public int getScamThreatIndex() {
        return scamThreatIndex;
    }
    public void setScamThreatIndex(int scamThreatIndex) {
        this.scamThreatIndex = scamThreatIndex;
    }

    public String getReasoning() {
        return reasoning;
    }
    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public String getRedFlags() {
        return redFlags;
    }
    public void setRedFlags(String redFlags) {
        this.redFlags = redFlags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

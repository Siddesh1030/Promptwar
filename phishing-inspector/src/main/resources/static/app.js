// View Navigation
function switchView(viewId) {
    document.querySelectorAll('.view').forEach(view => {
        view.classList.remove('active');
        view.classList.add('hidden');
    });
    
    document.querySelectorAll('.nav-links a').forEach(link => {
        link.classList.remove('active');
    });

    const targetView = document.getElementById(viewId);
    if (targetView) {
        targetView.classList.remove('hidden');
        targetView.classList.add('active');
    }

    const navLink = document.querySelector(`.nav-links a[onclick*="${viewId}"]`);
    if (navLink) {
        navLink.classList.add('active');
    }

    window.scrollTo(0, 0);
}

// Tab Switching
function switchTab(tabId, btnElement) {
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });
    
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    document.getElementById(tabId).classList.add('active');
    btnElement.classList.add('active');
}

// Character Counter
const offerText = document.getElementById('offerText');
const charCount = document.getElementById('charCount');
if(offerText) {
    offerText.addEventListener('input', () => {
        charCount.textContent = offerText.value.length;
    });
}

// File handling
let selectedFileBase64 = null;
let selectedFileName = null;

document.getElementById('fileInput').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
        selectedFileName = file.name;
        const reader = new FileReader();
        reader.onload = function(event) {
            selectedFileBase64 = event.target.result;
            const uploadArea = document.querySelector('.upload-area');
            uploadArea.innerHTML = `
                <i class="fa-solid fa-file-circle-check" style="color: #10b981;"></i>
                <h3>${file.name} ready for scan</h3>
                <button class="upload-btn" onclick="document.getElementById('fileInput').click()">Change File</button>
            `;
        };
        reader.readAsDataURL(file);
    }
});

// To allow clicking upload area button to trigger file input
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('upload-btn')) {
        document.getElementById('fileInput').click();
    }
});

// Form Submission / Scanning
async function startScan() {
    const textValue = document.getElementById('offerText').value;
    const urlValue = document.getElementById('companyUrl').value;
    const errorBox = document.getElementById('errorBox');
    const loadingPercentage = document.getElementById('loading-percentage');
    
    if (!textValue.trim() && !urlValue.trim() && !selectedFileBase64) {
        errorBox.textContent = 'Please provide either a Company URL, Offer Letter Text, or upload a file.';
        errorBox.classList.remove('hidden');
        return;
    }
    
    errorBox.classList.add('hidden');
    switchView('loading-view');
    loadingPercentage.textContent = '0%';
    
    // Animate Loading Steps and Percentage
    const steps = [
        document.getElementById('step1'),
        document.getElementById('step2'),
        document.getElementById('step3'),
        document.getElementById('step4'),
        document.getElementById('step5')
    ];
    
    let currentStep = 0;
    let percentage = 0;
    
    // Rapidly count up to 90% over about 2 seconds
    const percentageInterval = setInterval(() => {
        if (percentage < 90) {
            percentage += Math.floor(Math.random() * 5) + 2;
            if (percentage > 90) percentage = 90;
            loadingPercentage.textContent = percentage + '%';
        }
    }, 100);

    const stepInterval = setInterval(() => {
        if (currentStep > 0 && currentStep < steps.length) {
            steps[currentStep-1].classList.remove('active');
            steps[currentStep-1].classList.add('completed');
            steps[currentStep-1].querySelector('i').className = 'fa-solid fa-check';
        }
        
        if (currentStep < steps.length) {
            steps[currentStep].classList.remove('pending');
            steps[currentStep].classList.add('active');
            steps[currentStep].querySelector('i').className = 'fa-solid fa-circle-notch fa-spin';
        }
        
        currentStep++;
    }, 800);

    try {
        const response = await fetch('/api/scan', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ 
                companyUrl: urlValue, 
                offerText: textValue,
                fileBase64: selectedFileBase64,
                fileName: selectedFileName
            })
        });
        
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.error || 'Failed to scan the offer');
        }
        
        // Complete the percentage and show results
        clearInterval(percentageInterval);
        loadingPercentage.textContent = '100%';
        
        setTimeout(() => {
            clearInterval(stepInterval);
            renderResults(data);
            switchView('result-view');
        }, 300);

    } catch (error) {
        clearInterval(percentageInterval);
        clearInterval(stepInterval);
        switchView('home-view');
        errorBox.textContent = error.message;
        errorBox.classList.remove('hidden');
    }
}

// Result Rendering
function renderResults(data) {
    // Determine Risk Level
    const index = data.scam_threat_index || 0;
    const meterScore = document.getElementById('meter-score');
    const riskLabel = document.getElementById('risk-label');
    const meterContainer = document.querySelector('.meter-container');
    const verdictBox = document.getElementById('verdict-box');
    
    meterScore.textContent = index;
    
    // Clear previous risk classes
    meterContainer.classList.remove('risk-low', 'risk-moderate', 'risk-high');
    
    let severityClass = '';
    if (index < 40) {
        riskLabel.textContent = 'Low Risk';
        meterContainer.classList.add('risk-low');
        severityClass = 'badge-low';
        
        verdictBox.innerHTML = '<i class="fa-solid fa-check-circle"></i> <strong>Final Verdict:</strong> No major red flags detected, but always remain cautious.';
        verdictBox.style.background = '#d1fae5';
        verdictBox.style.borderColor = '#6ee7b7';
        verdictBox.style.color = '#065f46';
        verdictBox.classList.remove('hidden');
    } else if (index < 70) {
        riskLabel.textContent = 'Moderate Risk';
        meterContainer.classList.add('risk-moderate');
        severityClass = 'badge-moderate';
        
        verdictBox.innerHTML = '<i class="fa-solid fa-circle-exclamation"></i> <strong>Final Verdict:</strong> This offer has suspicious elements. Verify it officially before proceeding.';
        verdictBox.style.background = '#fef3c7';
        verdictBox.style.borderColor = '#fde68a';
        verdictBox.style.color = '#92400e';
        verdictBox.classList.remove('hidden');
    } else {
        riskLabel.textContent = 'High Risk';
        meterContainer.classList.add('risk-high');
        severityClass = 'badge-high';
        
        verdictBox.innerHTML = '<i class="fa-solid fa-triangle-exclamation"></i> <strong>Final Verdict:</strong> This offer letter did not officially come from the company and is highly likely a scam.';
        verdictBox.style.background = '#fee2e2';
        verdictBox.style.borderColor = '#fca5a5';
        verdictBox.style.color = '#991b1b';
        verdictBox.classList.remove('hidden');
    }

    // Set Circular Meter
    const circle = document.getElementById('meter-path');
    circle.style.strokeDasharray = `${index}, 100`;

    // Render Evidence List
    const evidenceList = document.getElementById('evidence-list');
    evidenceList.innerHTML = '';
    
    if (data.red_flags && data.red_flags.length > 0) {
        data.red_flags.forEach(flag => {
            evidenceList.innerHTML += `
                <div class="evidence-item">
                    <div class="evidence-icon"><i class="fa-solid fa-flag warning-text"></i></div>
                    <div class="evidence-content">
                        <h4>Security Indicator</h4>
                        <p>${flag}</p>
                        <span class="badge ${severityClass}">Detected</span>
                    </div>
                </div>
            `;
        });
    } else {
        evidenceList.innerHTML = `
            <div class="evidence-item">
                <div class="evidence-icon"><i class="fa-solid fa-check-circle safe-text"></i></div>
                <div class="evidence-content">
                    <h4>No Red Flags</h4>
                    <p>${data.reasoning || "The system did not detect overt red flags."}</p>
                    <span class="badge badge-low">Safe</span>
                </div>
            </div>
        `;
    }

    // Render Domain Info
    document.getElementById('domain-name').textContent = document.getElementById('companyUrl').value || 'Not provided';
    document.getElementById('domain-age').textContent = document.getElementById('companyUrl').value ? 'Requires advanced lookup' : 'N/A';
    
    // Render Highlighted Evidence (Mock feature based on reasoning)
    const highlights = document.getElementById('doc-highlights');
    if (document.getElementById('offerText').value.trim() && data.red_flags && data.red_flags.length > 0) {
        highlights.innerHTML = `
            <div class="highlight-snippet">
                "... <span class="highlight-text">suspicious phrasing</span> detected ..."
                <span class="badge ${severityClass}" style="float:right">AI Flagged</span>
            </div>
            <p>${data.reasoning}</p>
        `;
    } else {
        highlights.innerHTML = `<p>No specific text anomalies highlighted.</p>`;
    }
    
    // Render AI Suggestions
    const recGrid = document.getElementById('rec-grid');
    if (data.suggestions && data.suggestions.length > 0) {
        recGrid.innerHTML = '';
        data.suggestions.forEach(suggestion => {
            const iconClass = index < 40 ? 'fa-check-circle safe-text' : 'fa-circle-exclamation warning-text';
            recGrid.innerHTML += `
                <div class="rec-item">
                    <i class="fa-solid ${iconClass}"></i>
                    <p>${suggestion}</p>
                </div>
            `;
        });
    }
}

# Phishing Inspector

A single-page security scanner that analyzes job offer text and URLs to detect scams, built for a hackathon.

## Tech Stack
- **Backend**: Java 17, Spring Boot, Maven
- **Frontend**: HTML, CSS, Vanilla JavaScript
- **AI Integration**: Google Gemini API via native Java `HttpClient`
- **Domain Verification**: Apache commons-net `WhoisClient`

## Features
- **Minimalist UI**: Clean, flat, and professional security tool design.
- **Accessibility**: Inputs and buttons have explicit `aria-labels` for passing automated accessibility scoring.
- **Domain Verification**: Retrieves WHOIS data to extract domain age.
- **AI Analysis**: Uses Google Gemini to detect payment demands (e.g. equipment fees), poor grammar, and other red flags.
- **Security**: Strict handling of `.env` files for API keys, never hardcoded in source.

## Setup Instructions

1. **Environment Variables**
   Update the `.env` file in the root directory and add your Google Gemini API key:
   ```env
   GEMINI_API_KEY=your_actual_api_key_here
   ```

2. **Run Locally**
   You can build and run the application using Maven:
   ```bash
   mvn clean package
   java -jar target/phishing-inspector-0.0.1-SNAPSHOT.jar
   ```
   The application will be available at `http://localhost:8080`.

## Testing
Run unit tests to ensure the main API controller returns a valid HTTP 200 response:
```bash
mvn test
```

## Deployment to Google Cloud Run

This project is containerized and ready to be deployed to Google Cloud Run. Cloud Run automatically injects the `PORT` environment variable which the container binds to.

1. Authenticate with Google Cloud:
   ```bash
   gcloud auth login
   gcloud config set project [YOUR_PROJECT_ID]
   ```

2. Deploy using the exact `gcloud run deploy` command (building from source):
   ```bash
   gcloud run deploy phishing-inspector \
       --source . \
       --platform managed \
       --region us-central1 \
       --allow-unauthenticated \
       --set-env-vars GEMINI_API_KEY="your_actual_api_key_here"
   ```

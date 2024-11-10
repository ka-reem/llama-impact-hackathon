package game;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.IOException;

public class TogetherAIClient {
    private final String apiKey;
    private final HttpClient httpClient;
    private static final String API_URL = "https://api.together.xyz/v1/chat/completions";

    public TogetherAIClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String generateResponse(String prompt) throws IOException, InterruptedException {
        String requestBody = String.format(
            "{" +
            "\"model\": \"meta-llama/Meta-Llama-3-70B-Instruct-Turbo\"," +
            "\"messages\": [{\"role\": \"system\", \"content\": \"You are a helpful AI assistant.\"}, " +
                         "{\"role\": \"user\", \"content\": \"%s\"}]," +
            "\"temperature\": 0.7," +
            "\"max_tokens\": 1024" +
            "}", prompt.replace("\"", "\\\""));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());

        String responseBody = response.body();
        System.out.println("Debug - Raw response: " + responseBody);

        // Simplified response parsing
        if (response.statusCode() != 200) {
            System.err.println("API request failed with status " + response.statusCode());
            return "Error: Request failed with status " + response.statusCode();
        }

        if (responseBody.contains("\"error\":")) {
            return "Error: API returned an error response";
        }

        try {
            // Extract content using basic string operations
            int contentStart = responseBody.indexOf("\"content\":\"") + 11;
            int contentEnd = responseBody.indexOf("\"", contentStart);
            if (contentStart > 0 && contentEnd > 0) {
                return responseBody.substring(contentStart, contentEnd)
                    .replace("\\\"", "\"")
                    .replace("\\n", "\n")
                    .replace("\\\\", "\\");
            }
            return "Error: Could not parse response content";
        } catch (Exception e) {
            System.err.println("Error parsing response: " + e.getMessage());
            return "Error: Failed to process response";
        }
    }
}

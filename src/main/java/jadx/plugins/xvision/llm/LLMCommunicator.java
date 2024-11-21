package jadx.plugins.xvision.llm;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import jadx.plugins.xvision.utils.XVisionConstants;

public interface LLMCommunicator {
    String sendRequest(String prompt) throws IOException, InterruptedException;

    class GPT4Communicator implements LLMCommunicator {
        private final String apiKey;
        private final HttpClient httpClient;

        public GPT4Communicator(String apiKey) {
            this.apiKey = apiKey;
            this.httpClient = HttpClient.newHttpClient();
        }

        @Override
        public String sendRequest(String prompt) throws IOException, InterruptedException {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", XVisionConstants.GPT4_MODEL);

            JsonArray messages = new JsonArray();
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", XVisionConstants.SYSTEM_CONTENT);
            messages.add(systemMessage);

            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", prompt);
            messages.add(userMessage);

            requestBody.add("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(XVisionConstants.OPENAI_API_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();
            System.out.println("GPR Request: " + request);  // Print the request
            System.out.println("GPT Request Body: {\"prompt\": \"" + prompt + "\"}");

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("API request failed with status code: " + response.statusCode() +
                        "\nResponse: " + response.body());
            }

            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices.size() > 0) {
                JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                return message.get("content").getAsString();
            } else {
                throw new RuntimeException("No choices found in the response: " + response.body());
            }
        }
    }

    class ClaudeCommunicator implements LLMCommunicator {
        private final String apiKey;
        private final HttpClient httpClient;

        public ClaudeCommunicator(String apiKey) {
            this.apiKey = apiKey;
            this.httpClient = HttpClient.newHttpClient();
        }

        @Override
        public String sendRequest(String prompt) throws IOException, InterruptedException {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", XVisionConstants.CLAUDE_MODEL);
            requestBody.addProperty("max_tokens", 1024);
            JsonArray messages = new JsonArray();
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", prompt);
            messages.add(userMessage);
            requestBody.add("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(XVisionConstants.CLAUDE_API_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("X-API-Key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();
           System.out.println("Claude Request: " + request);  // Print the request
           System.out.println("Claude Request Body: {\"prompt\": \"" + prompt + "\"}");
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                JsonObject errorResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                if (errorResponse.has("error")) {
                    JsonObject error = errorResponse.getAsJsonObject("error");
                    String errorType = error.get("type").getAsString();
                    String errorMessage = error.get("message").getAsString();
                    throw new IOException("API request failed with error: " + errorType + " - " + errorMessage);
                } else {
                    throw new IOException("API request failed with status code: " + response.statusCode() +
                            "\nResponse: " + response.body());
                }
            }

            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray content = jsonResponse.getAsJsonArray("content");

            if (content != null && content.size() > 0) {
                StringBuilder result = new StringBuilder();
                for (JsonElement element : content) {
                    JsonObject contentObject = element.getAsJsonObject();
                    if (contentObject.has("text")) {
                        result.append(contentObject.get("text").getAsString());
                    }
                }
                return result.toString();
            } else {
                throw new RuntimeException("No content found in the response: " + response.body());
            }
        }
    }

    class CustomLLMCommunicator implements LLMCommunicator {
        private final String endpoint;
        private final HttpClient httpClient;

        public CustomLLMCommunicator(String endpoint) {
            this.endpoint = endpoint;
            this.httpClient = HttpClient.newHttpClient();
        }

        @Override
        public String sendRequest(String prompt) throws IOException, InterruptedException {
            // Implement the custom request logic based on the provided endpoint
            // You may need to adjust the request body, headers, and response parsing
            // based on the API specifications of the custom LLM

            // Example implementation (replace with your custom logic)
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"prompt\": \"" + prompt + "\"}"))
                    .build();

//            System.out.println("Custom Request: " + request);  // Print the request
//            System.out.println("Custom Request Body: {\"prompt\": \"" + prompt + "\"}");

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

//            System.out.println("Custom Response: " + response);  // Print the response
//            System.out.println("Custom Response Body: " + response.body());

            if (response.statusCode() != 200) {
                throw new IOException("API request failed with status code: " + response.statusCode() +
                        "\nResponse: " + response.body());
            }

            return response.body();
        }
    }
}
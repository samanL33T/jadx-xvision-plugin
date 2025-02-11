package jadx.plugins.xvision.llm;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import jadx.plugins.xvision.utils.XVisionConstants;

public interface LLMCommunicator {
    String sendRequest(String prompt) throws IOException;

    class GPT4Communicator implements LLMCommunicator {
        private final String apiKey;
        private final HttpClient httpClient;

        public GPT4Communicator(String apiKey) {
            this.apiKey = apiKey;
            this.httpClient = HttpClients.createDefault();
        }

        @Override
        public String sendRequest(String prompt) throws IOException {
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

            HttpPost request = new HttpPost(XVisionConstants.OPENAI_API_ENDPOINT);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Authorization", "Bearer " + apiKey);
            request.setEntity(new StringEntity(requestBody.toString()));

            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                throw new IOException("API request failed with status code: " + statusCode +
                        "\nResponse: " + EntityUtils.toString(response.getEntity()));
            }

            JsonObject jsonResponse = JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices.size() > 0) {
                JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                return message.get("content").getAsString();
            } else {
                throw new RuntimeException("No choices found in the response: " + EntityUtils.toString(response.getEntity()));
            }
        }
    }

    class ClaudeCommunicator implements LLMCommunicator {
        private final String apiKey;
        private final HttpClient httpClient;

        public ClaudeCommunicator(String apiKey) {
            this.apiKey = apiKey;
            this.httpClient = HttpClients.createDefault();
        }

        @Override
        public String sendRequest(String prompt) throws IOException {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", XVisionConstants.CLAUDE_MODEL);
            requestBody.addProperty("max_tokens", 1024);
            JsonArray messages = new JsonArray();
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", prompt);
            messages.add(userMessage);
            requestBody.add("messages", messages);

            HttpPost request = new HttpPost(XVisionConstants.CLAUDE_API_ENDPOINT);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("X-API-Key", apiKey);
            request.setHeader("anthropic-version", "2023-06-01");
            request.setEntity(new StringEntity(requestBody.toString()));

            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                throw new IOException("API request failed with status code: " + statusCode +
                        "\nResponse: " + EntityUtils.toString(response.getEntity()));
            }

            JsonObject jsonResponse = JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
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
                throw new RuntimeException("No content found in the response: " + EntityUtils.toString(response.getEntity()));
            }
        }
    }

    class DeepSeekR1Communicator implements LLMCommunicator {
        private final String apiKey;
        private final HttpClient httpClient;
    
        public DeepSeekR1Communicator(String apiKey) {
            this.apiKey = apiKey;
            this.httpClient = HttpClients.createDefault();
        }
    
        @Override
        public String sendRequest(String prompt) throws IOException {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", XVisionConstants.DEEPSEEK_R1_MODEL);
            
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
            requestBody.addProperty("temperature", 0.7);
            requestBody.addProperty("max_tokens", 4096);
    
            return sendDeepSeekRequest(requestBody);
        }
    
        private String sendDeepSeekRequest(JsonObject requestBody) throws IOException {
            HttpPost request = new HttpPost(XVisionConstants.DEEPSEEK_API_ENDPOINT);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Authorization", "Bearer " + apiKey);
            request.setEntity(new StringEntity(requestBody.toString()));
    
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
    
            if (statusCode != 200) {
                throw new IOException("API request failed with status code: " + statusCode +
                        "\nResponse: " + EntityUtils.toString(response.getEntity()));
            }
    
            JsonObject jsonResponse = JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices.size() > 0) {
                JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                return message.get("content").getAsString();
            } else {
                throw new RuntimeException("No choices found in the response");
            }
        }
    }
    
    class DeepSeekV3Communicator implements LLMCommunicator {
        private final String apiKey;
        private final HttpClient httpClient;
    
        public DeepSeekV3Communicator(String apiKey) {
            this.apiKey = apiKey;
            this.httpClient = HttpClients.createDefault();
        }
    
        @Override
        public String sendRequest(String prompt) throws IOException {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", XVisionConstants.DEEPSEEK_V3_MODEL);
            
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
            requestBody.addProperty("temperature", 0.7);
            requestBody.addProperty("max_tokens", 4096);
    
            return sendDeepSeekRequest(requestBody);
        }
    
        private String sendDeepSeekRequest(JsonObject requestBody) throws IOException {
            HttpPost request = new HttpPost(XVisionConstants.DEEPSEEK_API_ENDPOINT);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Authorization", "Bearer " + apiKey);
            request.setEntity(new StringEntity(requestBody.toString()));
    
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
    
            if (statusCode != 200) {
                throw new IOException("API request failed with status code: " + statusCode +
                        "\nResponse: " + EntityUtils.toString(response.getEntity()));
            }
    
            JsonObject jsonResponse = JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices.size() > 0) {
                JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                return message.get("content").getAsString();
            } else {
                throw new RuntimeException("No choices found in the response");
            }
        }
    }

    class CustomLLMCommunicator implements LLMCommunicator {
        private final String endpoint;
        private final String apiKey;
        private final String model;

        private final HttpClient httpClient;

        public CustomLLMCommunicator(String endpoint, String apikey, String model) {
            this.apiKey = apikey;
            this.endpoint = endpoint;
            this.model = model;
            this.httpClient = HttpClients.createDefault();
        }

        @Override
        public String sendRequest(String prompt) throws IOException {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);

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

            HttpPost request = new HttpPost(endpoint+"/chat/completions");
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Authorization", "Bearer " + apiKey);
            request.setEntity(new StringEntity(requestBody.toString()));

            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                throw new IOException("API request failed with status code: " + statusCode +
                        "\nResponse: " + EntityUtils.toString(response.getEntity()));
            }

            JsonObject jsonResponse = JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices.size() > 0) {
                JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                return message.get("content").getAsString();
            } else {
                throw new RuntimeException("No choices found in the response: " + EntityUtils.toString(response.getEntity()));
            }
        }
    }
}
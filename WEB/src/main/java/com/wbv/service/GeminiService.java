package com.wbv.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wbv.config.GeminiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class GeminiService {

    private final RestTemplate restTemplate;
    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper;

    @Autowired
    public GeminiService(RestTemplate restTemplate, GeminiConfig geminiConfig, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.geminiConfig = geminiConfig;
        this.objectMapper = objectMapper;
    }

    public String generateRecipe(String ingredients, String dietary, String cuisine) {
        String apiKey = geminiConfig.getApiKey();
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;
        String prompt = String.format(
                "Suggest a recipe just using the following ingredients: %s. Dietary preferences: %s. Preferred cuisine: %s.\n" +
                        "Make sure to send it in a JSON format with no extra text.\n" +
                        "JSON will contain the following keys: recipe (text), ingredients (ARRAY), instructions (text).\n" +
                        "Send the JSON as plaintext.\n Note : I don't have any other ingredients. (if not possible suggest in ingredients section)",
                ingredients, dietary, cuisine
        );

        String requestBody = String.format("{\"contents\": [{\"parts\":[{\"text\": \"%s\"}]}]}", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        org.springframework.http.HttpEntity<String> requestEntity = new org.springframework.http.HttpEntity<>(requestBody, headers);

        try {
            String response = restTemplate.postForObject(apiUrl, requestEntity, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (root.has("candidates") && root.get("candidates").isArray() && !root.get("candidates").isEmpty()) {
                JsonNode content = root.get("candidates").get(0).get("content");
                if (content.has("parts") && content.get("parts").isArray() && !content.get("parts").isEmpty()) {
                    String rawText = content.get("parts").get(0).get("text").asText();

                    String cleanedJson = rawText
                            .replaceAll("(?i)```json", "")
                            .replaceAll("```", "")
                            .trim();

                    JsonNode jsonNode = objectMapper.readTree(cleanedJson);
                    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
                }
            }
            return "No recipe suggestions found.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating recipe.";
        }
    }

}

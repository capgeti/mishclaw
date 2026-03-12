import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Behandelt ausschließlich die HTTP-Kommunikation und das Stream-Parsing.
 */
public class OllamaClient {
    private final String url;
    private final String modelName;
    private final ObjectMapper mapper;
    private final HttpClient httpClient;

    // Zustand für den aktuellen Stream (vermeidet Parameterübergabe-Chaos)
    private StringBuilder currentContentBuilder;
    private JsonNode currentToolCalls;

    public OllamaClient(String url, String modelName, ObjectMapper mapper) {
        this.url = url;
        this.modelName = modelName;
        this.mapper = mapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public OllamaResult sendChatRequest(ChatContext context, ArrayNode tools, Consumer<String> onChunkReceived) throws Exception {
        resetStreamState();
        String payload = buildJsonPayload(context, tools);
        HttpRequest request = buildHttpRequest(payload);

        processResponseStream(request, onChunkReceived);

        return new OllamaResult(currentContentBuilder.toString(), currentToolCalls);
    }

    private void resetStreamState() {
        this.currentContentBuilder = new StringBuilder();
        this.currentToolCalls = null;
    }

    private String buildJsonPayload(ChatContext context, ArrayNode tools) throws Exception {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("model", modelName);
        payload.put("stream", true);
        payload.set("messages", context.getMessages());
        payload.set("tools", tools);
        return mapper.writeValueAsString(payload);
    }

    private HttpRequest buildHttpRequest(String jsonPayload) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();
    }

    private void processResponseStream(HttpRequest request, Consumer<String> onChunkReceived) throws Exception {
        HttpResponse<Stream<String>> response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());

        response.body()
                .filter(line -> !line.trim().isEmpty())
                .forEach(line -> parseStreamLine(line, onChunkReceived));
    }

    private void parseStreamLine(String line, Consumer<String> onChunkReceived) {
        try {
            JsonNode jsonLine = mapper.readTree(line);
            if (!jsonLine.has("message")) return;

            JsonNode msg = jsonLine.get("message");
            extractContent(msg, onChunkReceived);
            extractToolCalls(msg);

        } catch (Exception e) {
            System.err.println("\n[Parse-Fehler im Stream]: " + e.getMessage());
        }
    }

    private void extractContent(JsonNode msg, Consumer<String> onChunkReceived) {
        if (msg.has("content") && !msg.get("content").isNull()) {
            String chunk = msg.get("content").asText();
            currentContentBuilder.append(chunk);
            onChunkReceived.accept(chunk);
        }
    }

    private void extractToolCalls(JsonNode msg) {
        if (msg.has("tool_calls")) {
            currentToolCalls = msg.get("tool_calls");
        }
    }
}
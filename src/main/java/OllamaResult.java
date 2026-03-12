import com.fasterxml.jackson.databind.JsonNode;

/**
 * Ein Datenübertragungsobjekt (DTO), das die geparste Antwort
 * des Ollama-Clients an den Orchestrator zurückgibt.
 */
public class OllamaResult {
    private final String content;
    private final JsonNode toolCalls;

    public OllamaResult(String content, JsonNode toolCalls) {
        this.content = content;
        this.toolCalls = toolCalls;
    }

    public String getContent() {
        return content;
    }

    public JsonNode getToolCalls() {
        return toolCalls;
    }

    public boolean hasToolCalls() {
        return toolCalls != null && toolCalls.isArray() && !toolCalls.isEmpty();
    }
}
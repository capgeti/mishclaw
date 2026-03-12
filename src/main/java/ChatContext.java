import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Versteckt die Komplexität der JSON-Manipulation für den Gesprächsverlauf.
 * Jede Methode hat genau eine Aufgabe: Einen bestimmten Nachrichtentyp hinzuzufügen.
 */
public class ChatContext {
    private final ObjectMapper mapper;
    private final ArrayNode messages;

    public ChatContext(ObjectMapper mapper) {
        this.mapper = mapper;
        this.messages = mapper.createArrayNode();
    }

    public void addSystemMessage(String content) {
        messages.add(createMessageNode("system", content));
    }

    public void addUserMessage(String content) {
        messages.add(createMessageNode("user", content));
    }

    public void addAssistantMessage(String content, JsonNode toolCalls) {
        ObjectNode msg = createMessageNode("assistant", content);
        if (toolCalls != null) {
            msg.set("tool_calls", toolCalls);
        }
        messages.add(msg);
    }

    public void addToolResultMessage(String toolName, String content) {
        ObjectNode msg = createMessageNode("tool", content);
        msg.put("name", toolName);
        messages.add(msg);
    }

    public ArrayNode getMessages() {
        return messages;
    }

    private ObjectNode createMessageNode(String role, String content) {
        ObjectNode node = mapper.createObjectNode();
        node.put("role", role);
        node.put("content", content);
        return node;
    }
}
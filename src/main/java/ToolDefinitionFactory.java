import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Fabrik-Klasse zur Erstellung von Tool-Definitionen.
 * Trennt die JSON-Schemagenerierung von der restlichen Logik.
 */
public class ToolDefinitionFactory {

    private final ObjectMapper mapper;

    public ToolDefinitionFactory(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public ArrayNode createRunTerminalTool() {
        ObjectNode tool = mapper.createObjectNode();
        tool.put("type", "function");
        tool.set("function", buildFunctionNode());

        ArrayNode tools = mapper.createArrayNode();
        tools.add(tool);
        return tools;
    }

    private ObjectNode buildFunctionNode() {
        ObjectNode function = mapper.createObjectNode();
        function.put("name", "run_terminal");
        function.put("description", "Führt einen Kommandozeilen-Befehl im Terminal des Betriebssystems aus.");
        function.set("parameters", buildParametersNode());
        return function;
    }

    private ObjectNode buildParametersNode() {
        ObjectNode commandProperty = mapper.createObjectNode();
        commandProperty.put("type", "string");
        commandProperty.put("description", "Der auszuführende Befehl (z.B. 'dir' oder 'ls -la')");

        ObjectNode properties = mapper.createObjectNode();
        properties.set("command", commandProperty);

        ArrayNode required = mapper.createArrayNode();
        required.add("command");

        ObjectNode parameters = mapper.createObjectNode();
        parameters.put("type", "object");
        parameters.set("properties", properties);
        parameters.set("required", required);
        return parameters;
    }
}
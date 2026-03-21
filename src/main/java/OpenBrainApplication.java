import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OpenBrainApplication {

    private final ConsoleUi ui;
    private final TerminalExecutor executor;
    private final OllamaClient client;
    private final ChatContext context;
    private final ArrayNode tools;

    public OpenBrainApplication() {
        var mapper = new ObjectMapper();

        this.ui = new ConsoleUi();
        this.executor = new TerminalExecutor();
        this.client = new OllamaClient("http://localhost:11434/api/chat", "qwen3.5:4b", mapper);
        this.context = new ChatContext(mapper);
        this.tools = new ToolDefinitionFactory(mapper).createRunTerminalTool();

        initSystemPrompt();
    }

    // --- Main Entry Point ---
    public static void main(String[] args) {
        new OpenBrainApplication().start();
    }

    private void initSystemPrompt() {
        try {
            String prompt = Files.readString(Path.of("src/main/resources/systemprompt.md"));
            context.addSystemMessage(prompt);
        } catch (IOException e) {
            ui.printError("Systemprompt konnte nicht geladen werden: " + e.getMessage());
            System.exit(1);
        }
    }

    public void start() {
        while (true) {
            var userInput = ui.readUserInput();

            if (userInput == null || userInput.equalsIgnoreCase("exit")) {
                break;
            }
            if (userInput.trim().isEmpty()) {
                continue;
            }

            context.addUserMessage(userInput);
            runAgenticLoop();
        }
    }

    private void runAgenticLoop() {
        var taskCompleted = false;

        while (!taskCompleted) {
            try {
                // Client aufrufen und Callback für den Stream übergeben
                var result = client.sendChatRequest(context, tools, ui::printAgentChunk);
                if (!result.getContent().isEmpty()) {
                    ui.printNewLine();
                }

                // Antwort in den Kontext aufnehmen
                context.addAssistantMessage(result.getContent(), result.getToolCalls());

                if (result.hasToolCalls()) {
                    handleTools(result.getToolCalls());
                } else {
                    taskCompleted = true; // Keine Tools mehr? Loop beenden.
                }

            } catch (Exception e) {
                ui.printError(e.getMessage());
                taskCompleted = true; // Bei Fehler sicherheitshalber abbrechen
            }
        }
    }

    private void handleTools(JsonNode toolCalls) {
        for (var toolCall : toolCalls) {
            var function = toolCall.get("function");
            var functionName = function.get("name").asText();

            if ("run_terminal".equals(functionName)) {
                var command = function.get("arguments").get("command").asText();
                ui.printSystem("🛠️ " + command);

                var terminalResult = executor.execute(command);

                context.addToolResultMessage(functionName, terminalResult);
            }
        }
    }
}
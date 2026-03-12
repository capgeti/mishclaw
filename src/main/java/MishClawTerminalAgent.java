import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class MishClawTerminalAgent {

    private final ConsoleUi ui;
    private final TerminalExecutor executor;
    private final OllamaClient client;
    private final ChatContext context;
    private final ArrayNode tools;

    public MishClawTerminalAgent() {
        var mapper = new ObjectMapper();

        this.ui = new ConsoleUi();
        this.executor = new TerminalExecutor();
        this.client = new OllamaClient("http://localhost:11434/api/chat", "qwen3.5:cloud", mapper);
        this.context = new ChatContext(mapper);
        this.tools = new ToolDefinitionFactory(mapper).createRunTerminalTool();

        initSystemPrompt();
    }

    // --- Main Entry Point ---
    public static void main(String[] args) {
        new MishClawTerminalAgent().start();
    }

    private void initSystemPrompt() {
        context.addSystemMessage("Du bist ein hilfreicher KI-Systemadministrator. " +
                "Du hast Zugriff auf ein Terminal. Nutze das Tool 'run_terminal', um Aufgaben zu lösen. " +
                "Warte auf das Ergebnis, bevor du die finale Antwort gibst. Lösche keine Daten oder beende keine Prozesse. Behandle die Daten mit vorsicht. Frag lieber nach, wenn du eine Kritisches Command ausführen möchtest.");
    }

    public void start() {
        ui.printInfo("=== MishClaw Terminal Agent gestartet ===");
        ui.printInfo("Tippe 'exit' um zu beenden. Für mehrzeilige Eingaben tippe '\\' am Ende der Zeile.\n");

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
                ui.printAgentPrefix();

                // Client aufrufen und Callback für den Stream übergeben
                var result = client.sendChatRequest(context, tools, ui::printAgentChunk);
                ui.printNewLine();

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
                ui.printSystem("   [🛠️ Tool Call erkannt] Führe aus: " + command);

                var terminalResult = executor.execute(command);
                ui.printSystem("   [✅ Tool Ergebnis gesammelt, sende zurück an LLM...]\n");

                context.addToolResultMessage(functionName, terminalResult);
            }
        }
    }
}
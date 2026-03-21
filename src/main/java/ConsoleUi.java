import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class ConsoleUi {

    private final Terminal terminal;
    private final LineReader lineReader;

    public ConsoleUi() {
        try {
            terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();

            lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Konnte Terminal nicht initialisieren: " + e.getMessage(), e);
        }
    }

    public String readUserInput() {
        try {
            return lineReader.readLine("❯ ");
        } catch (Exception e) {
            return null;
        }
    }

    public void printSystem(String text) {
        terminal.writer().print(text);
        terminal.writer().flush();
    }

    public void printError(String text) {
        terminal.writer().print("[Fehler]: " + text);
        terminal.writer().flush();
    }

    public void printAgentChunk(String chunk) {
        printSystem(chunk);
    }

    public void printNewLine() {
        terminal.writer().println();
        terminal.writer().flush();
    }

    public void close() {
        try {
            terminal.close();
        } catch (IOException e) {
            // Ignorieren
        }
    }
}
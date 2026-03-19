import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Kapselt alle JLine-Abhängigkeiten und Methoden für die Ein- und Ausgabe.
 */
public class ConsoleUi {
    private Terminal terminal;
    private LineReader lineReader;
    private String userPrompt;

    public ConsoleUi() {
        initTerminal();
        initLineReader();
        initUserPrompt();
    }

    private void initTerminal() {
        try {
            this.terminal = TerminalBuilder.builder()
                    .encoding(StandardCharsets.UTF_8 )
                    .system(true).build();
        } catch (IOException e) {
            throw new RuntimeException("Konnte Terminal nicht initialisieren", e);
        }
    }

    private void initLineReader() {
        DefaultParser parser = new DefaultParser();
        parser.setEscapeChars(new char[]{'\\'});

        this.lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .parser(parser)
                .variable(LineReader.SECONDARY_PROMPT_PATTERN, "%M> ")
                .build();
    }

    private void initUserPrompt() {
        this.userPrompt = new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN).bold())
                .append("🔹")
                .toAnsi(terminal);
    }

    public String readUserInput() {
        try {
            return lineReader.readLine(userPrompt);
        } catch (UserInterruptException e) {
            return null; // Wird als Abbruch-Signal verwendet
        }
    }

    public void printSystem(String text) {
        printColored(text, AttributedStyle.MAGENTA);
    }

    public void printError(String text) {
        printColored("\n[Fehler]: " + text, AttributedStyle.RED);
    }

    public void printAgentChunk(String chunk) {
        String coloredChunk = new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN))
                .append(chunk)
                .toAnsi(terminal);
        terminal.writer().print(coloredChunk);
        terminal.writer().flush();
    }

    public void printNewLine() {
        terminal.writer().println();
        terminal.writer().flush();
    }

    private void printColored(String text, int colorCode) {
        String coloredText = new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(colorCode))
                .append(text)
                .toAnsi(terminal);
        terminal.writer().println(coloredText);
        terminal.writer().flush();
    }
}
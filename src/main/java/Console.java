public interface Console {
    String readUserInput();

    void printSystem(String text);

    void printError(String text);

    void printAgentChunk(String chunk);

    void printNewLine();

    void close();
}

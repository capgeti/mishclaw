# OpenBrain Terminal Agent

Ein KI-gesteuerter Terminal-Agent, der lokale LLMs (über Ollama) nutzt, um Terminal-Befehle auszuführen.

## Überblick

OpenBrain ist ein Java-basierter Chat-Agent, der als hilfreicher KI-Systemadministrator agiert. Er kann Terminal-Befehle ausführen, um
Aufgaben zu lösen, wobei Sicherheitsrichtlinien eingehalten werden.

## Features

- **LLM-Integration**: Verbindet sich mit Ollama (standardmäßig qwen3.5:cloud)
- **Terminal-Ausführung**: Führt Shell-Befehle sicher aus
- **Chat-Kontext**: Verwaltet Gesprächsverlauf für kontextuelles Verständnis
- **Tool-Support**: Nutzt Function Calling für Terminal-Operationen
- **Sicherheitsrichtlinien**:
    - Löscht keine Daten
    - Beendet keine Prozesse ohne Nachfrage
    - Fragt bei kritischen Befehlen nach
    - Behandelt Daten mit Vorsicht

## Voraussetzungen

- Java 25
- Maven 3.x
- Ollama installiert und laufend (http://localhost:11434)
- Ein gezogenes LLM-Modell (z.B. qwen3.5:cloud)

## Installation

### 1. Repository klonen

```bash
git clone git@github.com:capgeti/openbrain.git
cd openbrain
```

### 2. Ollama sicherstellen

```bash
ollama serve
ollama pull qwen3.5:cloud
```

### 3. Projekt bauen

```bash
mvn clean package
mvn clean package -Pnative 
```

## Verwendung

### Starten des Agents

```bash
java -jar target/openbrain-0.1.0-SNAPSHOT.jar
./openbrain
```

### Interaktion

- Normale Eingaben: Einfach tippen und Enter drücken
- Mehrzeilige Eingaben: '\' am Ende der Zeile
- Beenden: 'exit' eingeben

## Architektur

### Kernkomponenten

| Klasse                | Beschreibung                        |
|-----------------------|-------------------------------------|
| OpenBrainApplication | Hauptsteuerung und Entry Point      |
| ConsoleUiLanterna             | Benutzeroberfläche für Input/Output |
| TerminalExecutor      | Führt Terminal-Befehle aus          |
| OllamaClient          | Kommunikation mit Ollama API        |
| ChatContext           | Verwaltet Gesprächsverlauf          |
| ToolDefinitionFactory | Erstellt Tool-Definitionen          |

## Dependencies

- **Jackson** (2.15.2) - JSON Verarbeitung
- **Apache Commons Exec** (1.3) - Prozessausführung
- **JLine** (3.25.1) - CLI Handling

## Sicherheitshinweise

Der Agent folgt strikten Sicherheitsrichtlinien:

- Keine destruktiven Befehle (rm -rf)
- Kein kill von Prozessen ohne Bestätigung
- Kritische Befehle erfordern Nachfrage
- Readonly-Operationen sind bevorzugt

---

**Erstellt von**: Michael Schwarz
**Version**: 0.1.0-SNAPSHOT

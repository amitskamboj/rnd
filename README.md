# Java AI Sample

A small Java project that shows how to call Claude — Anthropic's AI model —
from Java using the official `anthropic-java` SDK. It's meant as a guided
tour of the basics, not a production template.

## What it demonstrates

Running the app walks through four self-explained demos in
[`AiSampleApp.java`](src/main/java/com/example/aisample/AiSampleApp.java):

1. **Basic request** — send one message, read the text response.
2. **Adaptive thinking** — let the model reason step-by-step before
   answering, and print that reasoning alongside the final answer.
3. **Effort levels** — run the same prompt at `low` and `high` effort and
   compare output token counts, to see the cost/quality knob in action.
4. **Interactive chat** — a minimal REPL that keeps a running conversation
   history across turns, so you can have an actual back-and-forth.

## Prerequisites

- Java 17+
- Maven
- An Anthropic API key, exported as `ANTHROPIC_API_KEY`

```bash
export ANTHROPIC_API_KEY=sk-ant-...
```

## Running it

```bash
mvn -q exec:java
```

Or build a runnable jar:

```bash
mvn -q package
java -jar target/java-ai-sample.jar
```

Type `exit` (or Ctrl-D) to leave the interactive chat at the end.

## Where to look next

- [`AiSampleApp.java`](src/main/java/com/example/aisample/AiSampleApp.java) —
  every demo is a short, standalone method; read them top to bottom.
- The model used is `claude-opus-5` (`MODEL` constant at the top of the
  file) — swap it for `claude-sonnet-5` or `claude-haiku-4-5` to trade
  capability for speed/cost.
- [Anthropic Java SDK](https://github.com/anthropics/anthropic-sdk-java) for
  the full API surface (tool use, streaming, batches, structured output).

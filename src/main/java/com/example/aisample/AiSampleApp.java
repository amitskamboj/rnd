package com.example.aisample;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.ThinkingConfigAdaptive;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * A small tour of Claude, Anthropic's AI model family, through the official Java SDK.
 *
 * Run with: mvn -q exec:java
 * Requires the ANTHROPIC_API_KEY environment variable to be set.
 *
 * Each demo below is self-contained and prints an explanation of the concept
 * it exercises before showing the API call and its result.
 */
public class AiSampleApp {

    // The current top-tier Claude model. Swap this for "claude-sonnet-5" or
    // "claude-haiku-4-5" to trade capability for cost/latency.
    private static final String MODEL = "claude-opus-5";

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        printSection("1. Basic request", """
                The simplest way to use Claude: send a user message, get a
                response back. Every other feature below builds on this.""");
        basicRequest(client);

        printSection("2. Adaptive thinking", """
                Claude can reason step-by-step before answering. "Adaptive"
                thinking lets the model decide on its own how much reasoning
                a question needs, instead of you setting a fixed budget.""");
        adaptiveThinking(client);

        printSection("3. Effort levels", """
                The "effort" setting controls how much the model thinks and
                spends per response: low/medium/high/xhigh/max. Higher effort
                trades latency and tokens for more thorough answers.""");
        effortLevels(client);

        printSection("4. Interactive chat", """
                A minimal multi-turn loop: each turn appends the user's
                message and Claude's reply to a running conversation history,
                so the model has full context on every exchange. Type 'exit'
                to quit.""");
        interactiveChat(client);
    }

    /** Demo 1: one request, one response. */
    private static void basicRequest(AnthropicClient client) {
        MessageCreateParams params = MessageCreateParams.builder()
                .model(MODEL)
                .maxTokens(1024L)
                .addUserMessage("In two sentences, what makes Claude different from a plain chatbot?")
                .build();

        Message response = client.messages().create(params);
        printAssistantText(response);
    }

    /** Demo 2: adaptive thinking with a summarized view of the model's reasoning. */
    private static void adaptiveThinking(AnthropicClient client) {
        MessageCreateParams params = MessageCreateParams.builder()
                .model(MODEL)
                .maxTokens(2048L)
                .thinking(ThinkingConfigAdaptive.builder()
                        .display(ThinkingConfigAdaptive.Display.SUMMARIZED)
                        .build())
                .addUserMessage("A train leaves at 2pm going 60mph. Another leaves the " +
                        "same station at 3pm going 90mph in the same direction. " +
                        "What time does the second train catch the first?")
                .build();

        Message response = client.messages().create(params);
        for (ContentBlock block : response.content()) {
            block.thinking().ifPresent(t -> System.out.println("[thinking] " + t.thinking()));
            block.text().ifPresent(t -> System.out.println("[answer]   " + t.text()));
        }
    }

    /** Demo 3: same prompt, two effort levels, compare token spend. */
    private static void effortLevels(AnthropicClient client) {
        String prompt = "Summarize the plot of Romeo and Juliet in one sentence.";

        for (OutputConfig.Effort effort : List.of(OutputConfig.Effort.LOW, OutputConfig.Effort.HIGH)) {
            MessageCreateParams params = MessageCreateParams.builder()
                    .model(MODEL)
                    .maxTokens(1024L)
                    .outputConfig(OutputConfig.builder().effort(effort).build())
                    .addUserMessage(prompt)
                    .build();

            Message response = client.messages().create(params);
            System.out.println("--- effort=" + effort + " ---");
            printAssistantText(response);
            System.out.println("output tokens: " + response.usage().outputTokens());
        }
    }

    /** Demo 4: a bare-bones REPL that keeps conversation history across turns. */
    private static void interactiveChat(AnthropicClient client) {
        List<MessageParam> history = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.print("you> ");
                String line = in.readLine();
                if (line == null || line.equalsIgnoreCase("exit")) {
                    break;
                }
                if (line.isBlank()) {
                    continue;
                }

                history.add(MessageParam.builder()
                        .role(MessageParam.Role.USER)
                        .content(line)
                        .build());

                MessageCreateParams params = MessageCreateParams.builder()
                        .model(MODEL)
                        .maxTokens(1024L)
                        .messages(history)
                        .build();

                Message response = client.messages().create(params);
                String reply = response.content().stream()
                        .flatMap(b -> b.text().stream())
                        .map(t -> t.text())
                        .reduce("", (a, b) -> a + b);
                System.out.println("claude> " + reply);

                history.add(MessageParam.builder()
                        .role(MessageParam.Role.ASSISTANT)
                        .content(reply)
                        .build());
            }
        } catch (Exception e) {
            System.out.println("(chat ended: " + e.getMessage() + ")");
        }
    }

    private static void printAssistantText(Message response) {
        response.content().stream()
                .flatMap(block -> block.text().stream())
                .forEach(textBlock -> System.out.println(textBlock.text()));
    }

    private static void printSection(String title, String description) {
        System.out.println();
        System.out.println("== " + title + " ==");
        System.out.println(description);
        System.out.println();
    }
}

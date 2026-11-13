package hk.edu.polyu.comp.comp2021.crobs.view;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * Reads command lines from, and prints results to, the console.
 */
public final class ConsoleView {
    private final BufferedReader input;
    private final PrintWriter output;

    /**
     * Creates a view over the given streams.
     *
     * @param input the source of command lines
     * @param output where results are printed
     */
    public ConsoleView(BufferedReader input, PrintWriter output) {
        this.input = input;
        this.output = output;
    }

    /** @return input stram to read line, Return null on EOF.
     * The application owns resource lifetime.
     * @throws IOException when stream is closed or interrupted
     * */
    public String readLine() throws IOException {
        return input.readLine();
    }

    /**
     * Prints the {@code CROBS>>} prompt.
     */
    public void showPrompt() {
        output.print("CROBS>> ");
        output.flush();
    }

    /**
     * Prints a message followed by a line break.
     *
     * @param message the text to show
     */
    public void display(String message) {
        output.println(message);
        output.flush();
    }
}

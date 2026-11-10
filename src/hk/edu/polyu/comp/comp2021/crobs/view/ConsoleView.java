package hk.edu.polyu.comp.comp2021.crobs.view;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;

public final class ConsoleView {
    private final BufferedReader input;
    private final PrintWriter output;

    public ConsoleView(BufferedReader input, PrintWriter output) {
        this.input = input;
        this.output = output;
    }

    /** Return null on EOF. The application owns resource lifetime. */
    public String readLine() throws IOException {
        return input.readLine();
    }

    public void showPrompt() {
        output.print("CROBS>> ");
        output.flush();
    }

    public void display(String message) {
        output.println(message);
        output.flush();
    }
}

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
        throw new UnsupportedOperationException("TODO: implement input");
    }

    public void showPrompt() {
        // TODO: print and flush the prompt.
        throw new UnsupportedOperationException("TODO: implement prompt");
    }

    public void display(String message) {
        throw new UnsupportedOperationException("TODO: implement output");
    }
}

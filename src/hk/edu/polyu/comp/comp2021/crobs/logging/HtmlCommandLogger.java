package hk.edu.polyu.comp.comp2021.crobs.logging;

import java.io.IOException;
import java.nio.file.Path;

public final class HtmlCommandLogger implements CommandLogger {
    private final Path path;

    public HtmlCommandLogger(Path path) throws IOException {
        this.path = path;
        // TODO: open UTF-8 writer and handle initialization failures.
        throw new UnsupportedOperationException("TODO: initialize logger");
    }

    @Override
    public void log(String commandLine) throws IOException {
        // TODO: write escaped two-column HTML table and one-based operation index; flush.
        throw new UnsupportedOperationException("TODO: write command");
    }

    @Override
    public void close() throws IOException {
        // TODO: close owned writer; HTML logger must finish table/document.
        throw new UnsupportedOperationException("TODO: close logger safely");
    }
}

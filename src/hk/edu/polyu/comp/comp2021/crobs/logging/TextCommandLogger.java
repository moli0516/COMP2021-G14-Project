package hk.edu.polyu.comp.comp2021.crobs.logging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TextCommandLogger implements CommandLogger {
    private final Path path;
    private final BufferedWriter writer;

    public TextCommandLogger(Path path) throws IOException {
        this.path = path;
        this.writer = Files.newBufferedWriter(this.path, StandardCharsets.UTF_8);
    }

    @Override
    public void log(String commandLine) throws IOException {
        writer.write(commandLine);
        writer.newLine();
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
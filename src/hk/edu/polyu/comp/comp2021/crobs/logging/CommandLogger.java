package hk.edu.polyu.comp.comp2021.crobs.logging;

import java.io.IOException;

public interface CommandLogger extends AutoCloseable {
    void log(String commandLine) throws IOException;

    @Override
    void close() throws IOException;
}

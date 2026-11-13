package hk.edu.polyu.comp.comp2021.crobs.logging;

import java.io.IOException;

/**
 * Records executed command lines.
 */
public interface CommandLogger extends AutoCloseable {

    /**
     * Records one command line.
     *
     * @param commandLine the command exactly as typed
     * @throws IOException if writing fails
     */
    void log(String commandLine) throws IOException;

    @Override
    void close() throws IOException;
}

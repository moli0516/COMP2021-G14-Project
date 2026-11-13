package hk.edu.polyu.comp.comp2021.crobs.logging;

import java.io.IOException;
import java.nio.file.Path;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Logs commands as rows of an HTML table (index and command).
 */
public final class HtmlCommandLogger implements CommandLogger {
    private final BufferedWriter writer;
    private boolean closed;
    private int index;

    /**
     * Creates the log file and writes the HTML header.
     *
     * @param path the HTML file to write
     * @throws IOException if the file cannot be created
     */
    public HtmlCommandLogger(Path path) throws IOException {
        this.writer=Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        writer.write("<!DOCTYPE html>");
        writer.newLine();
        writer.write("<html lang='en'>" +
                    "<head><meta charset=\"UTF-8\">" +
                        "<style>table, th, td { border: 1px solid black; border-collapse: collapse;} </style>" +
                        "<title>CROBS Command Log</title>" +
                    "</head>" +
                "<body>");
        writer.newLine();
        writer.write("<table border=\"1\">");
        writer.newLine();
        writer.write("<tr><th>Index</th><th>Command</th></tr>");
        writer.newLine();
        writer.flush();
    }

    @Override
    public void log(String commandLine) throws IOException {
        index++;
        writer.write("<tr><td>"+index+"</td><td>"+escape(commandLine)+"</td></tr>");
        writer.newLine();
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        if(closed){
            return;
        }
        closed=true;
        try{
            writer.write("</table>");
            writer.newLine();
            writer.write("</body></html>");
            writer.newLine();
        }finally {
            writer.close();
        }
    }

    /**
     * Escapes characters that are special in HTML.
     *
     * @param text the raw text
     * @return the escaped text
     */
    static String escape(String text){
        return text.replace("&","&amp;").replace("<","&lt;")
        .replace(">","&gt;").replace("\"","&quot;")
        .replace("'","&#39;");
    }
}

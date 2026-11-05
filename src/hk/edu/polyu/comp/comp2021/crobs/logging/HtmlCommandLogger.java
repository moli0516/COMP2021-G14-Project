package hk.edu.polyu.comp.comp2021.crobs.logging;

import java.io.IOException;
import java.nio.file.Path;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class HtmlCommandLogger implements CommandLogger {
    private final Path path;
    private final BufferedWriter writer;
    private boolean closed;
    private int index;

    public HtmlCommandLogger(Path path) throws IOException {
        this.path = path;
        this.writer=Files.newBufferedWriter(this.path, StandardCharsets.UTF_8);
        writer.write("<!DOCTYPE html>");
        writer.newLine();
        writer.write("<html><head><meta charset=\"UTF-8\"><titles><CROBS Command Loog</title></head><body>>");
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
        writer.write("<tr><td>"+index"</td><td>"+escape(commandLine)+"</td></tr>");
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
            wrietr.newLine();
        }
        writer.close();
    }
    static String escape(String text){
        String res=text.replace("&","&amp;").replace("<","&lt;")
        .replace(">","&gt;").replace("\"","&quot;")
        .replace("'","&#39");
        return res;
    }
}

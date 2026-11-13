package hk.edu.polyu.comp.comp2021.crobs;

import hk.edu.polyu.comp.comp2021.crobs.controller.CommandParser;
import hk.edu.polyu.comp.comp2021.crobs.controller.CommandProcessor;
import hk.edu.polyu.comp.comp2021.crobs.logging.CommandLogger;
import hk.edu.polyu.comp.comp2021.crobs.logging.HtmlCommandLogger;
import hk.edu.polyu.comp.comp2021.crobs.logging.TextCommandLogger;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.view.ConsoleView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

    /** Command-line entry point of the Campus Room Booking System. 
    */
public final class Application {
    private static final String USAGE = "Usage: java hk.edu.polyu.comp.comp2021.crobs.Application -html <file> -txt <file>";
    private Application() {
    }

    /**
     * Starts the console application.
     *
     * @param args {@code -html <file> -txt <file>}
     */
    public static void main(String[] args) {
        Path htmlPath = null;
        Path txtPath = null;

        try {
            if (args.length == 4) {
                for (int i = 0; i < 4; i += 2) {
                    String flag = args[i];
                    String value = args[i + 1];

                    if (value.trim().isEmpty()) {
                        break;
                    }

                    boolean isHtml = "-html".equals(flag) && htmlPath == null;
                    boolean isTxt = "-txt".equals(flag) && txtPath == null;

                    if (!isHtml && !isTxt) {
                        break;
                    }

                    Path path = Paths.get(value).toAbsolutePath().normalize();

                    if (isHtml) {
                        htmlPath = path;
                    } else {
                        txtPath = path;
                    }
                }
            }
        } catch (InvalidPathException e) {
            System.err.println(USAGE);
            System.exit(1);
            return;
        }

        if (htmlPath == null || txtPath == null) {
            System.err.println(USAGE);
            System.exit(1);
            return;
        }
        if (htmlPath.equals(txtPath)){
            System.err.println("HTML and TXT log files must be different files.");
            System.exit(1);
            return;
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in,StandardCharsets.UTF_8));
        PrintWriter out= new PrintWriter(System.out, true);
        ConsoleView view= new ConsoleView(in,out);
        try(CommandLogger htmlLogger=new HtmlCommandLogger(htmlPath);
            CommandLogger txtLogger = new TextCommandLogger(txtPath)){
                run(view, new CommandProcessor(new CommandParser(new CROBS())),htmlLogger,txtLogger);
        }catch(IOException e){
            System.err.println("Log file error: "+e.getMessage());
            System.exit(1);
        }
    }
    private static void run(ConsoleView view, CommandProcessor processor,
                            CommandLogger htmlLogger, CommandLogger txtLogger)throws IOException{
                                while(!processor.isQuitRequested()){
                                    view.showPrompt();
                                    String line = view.readLine();
                                    if (line==null){
                                        break;
                                    }
                                    if (line.trim().isEmpty()){
                                        continue;
                                    }
                                    htmlLogger.log(line);
                                    txtLogger.log(line);
                                    view.display(processor.process(line));
                                }
                            }
}

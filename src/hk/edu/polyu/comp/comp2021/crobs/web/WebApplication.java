package hk.edu.polyu.comp.comp2021.crobs.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import hk.edu.polyu.comp.comp2021.crobs.controller.CommandParser;
import hk.edu.polyu.comp.comp2021.crobs.controller.CommandProcessor;
import hk.edu.polyu.comp.comp2021.crobs.logging.CommandLogger;
import hk.edu.polyu.comp.comp2021.crobs.logging.HtmlCommandLogger;
import hk.edu.polyu.comp.comp2021.crobs.logging.TextCommandLogger;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/** Local single-session GUI adapter. Run from the repository root. */
public final class WebApplication {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 8080;
    private static final String SERVER_HOST =
            SERVER_ADDRESS + ":" + SERVER_PORT;
    private static final String LOCALHOST_HOST =
            "localhost:" + SERVER_PORT;

    private static final int READ_BUFFER_BYTES = 1024;
    private static final int MAX_COMMAND_BYTES = 8192;
    private static final int SHUTDOWN_DELAY_SECONDS = 1;

    private static final int ARGUMENT_COUNT = 4;
    private static final int HTML_FLAG_INDEX = 0;
    private static final int HTML_PATH_INDEX = 1;
    private static final int TXT_FLAG_INDEX = 2;
    private static final int TXT_PATH_INDEX = 3;

    /**
     * Prevents instantiation of this application entry point.
     */
    private WebApplication() { }

    /**
     * Starts a loopback-only HTTP UI using assets under {@code webui/}.
     * All browser tabs share one CROBS login session. Commands and log writes
     * are serialized; a quit command or an I/O failure signals server shutdown.
     *
     * @param args exactly {@code -html <html-log-path> -txt <text-log-path>}
     * @throws IllegalArgumentException if the argument layout is invalid
     *         or the log paths refer to the same file
     * @throws IOException if assets, log files, or the HTTP server
     *         cannot be accessed
     */
    public static void main(String[] args) throws IOException {
        if (args.length != ARGUMENT_COUNT
                || !"-html".equals(args[HTML_FLAG_INDEX])
                || !"-txt".equals(args[TXT_FLAG_INDEX])) {
            throw new IllegalArgumentException(
                    "Usage: WebApplication -html file -txt file");
        }

        Path html = Paths.get(args[HTML_PATH_INDEX])
                .toAbsolutePath().normalize();
        Path txt = Paths.get(args[TXT_PATH_INDEX])
                .toAbsolutePath().normalize();

        if (html.equals(txt)
                || (Files.exists(html) && Files.exists(txt)
                && Files.isSameFile(html, txt))) {
            throw new IllegalArgumentException(
                    "Log files must be different");
        }

        String page = new String(
                Files.readAllBytes(Paths.get("webui/index.html")),
                StandardCharsets.UTF_8);
        String css = new String(
                Files.readAllBytes(Paths.get("webui/style.css")),
                StandardCharsets.UTF_8);
        String js = new String(
                Files.readAllBytes(Paths.get("webui/app.js")),
                StandardCharsets.UTF_8);

        String token = UUID.randomUUID().toString();
        String document = page.replace("TOKEN_PLACEHOLDER", token);

        HttpServer server = HttpServer.create(
                new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT), 0);

        try (CommandLogger htmlLog = new HtmlCommandLogger(html);
             CommandLogger txtLog = new TextCommandLogger(txt)) {
            CommandProcessor processor =
                    new CommandProcessor(new CommandParser(new CROBS()));
            Object lock = new Object();
            CountDownLatch stopped = new CountDownLatch(1);

            server.createContext("/", exchange -> {
                try {
                    String host =
                            exchange.getRequestHeaders().getFirst("Host");

                    if (!SERVER_HOST.equals(host)
                            && !LOCALHOST_HOST.equals(host)) {
                        send(exchange, HttpURLConnection.HTTP_FORBIDDEN,
                                "text/plain", "Invalid host");
                        return;
                    }

                    String path = exchange.getRequestURI().getPath();

                    if ("/api/command".equals(path)) {
                        if (!"POST".equals(exchange.getRequestMethod())) {
                            send(exchange, HttpURLConnection.HTTP_BAD_METHOD,
                                    "text/plain", "POST required");
                            return;
                        }

                        if (!token.equals(exchange.getRequestHeaders()
                                .getFirst("X-CROBS-Token"))) {
                            send(exchange, HttpURLConnection.HTTP_FORBIDDEN,
                                    "text/plain", "Invalid request token");
                            return;
                        }

                        String command = readCommand(exchange);

                        if (command.trim().isEmpty()
                                || command.indexOf('\n') >= 0
                                || command.indexOf('\r') >= 0) {
                            send(exchange, HttpURLConnection.HTTP_BAD_REQUEST,
                                    "text/plain",
                                    "One nonblank command required");
                            return;
                        }

                        synchronized (lock) {
                            htmlLog.log(command);
                            txtLog.log(command);

                            String output = processor.process(command);
                            send(exchange, HttpURLConnection.HTTP_OK,
                                    "text/plain", output);

                            if (processor.isQuitRequested()) {
                                stopped.countDown();
                            }
                        }
                    } else if ("GET".equals(exchange.getRequestMethod())) {
                        if ("/".equals(path)) {
                            send(exchange, HttpURLConnection.HTTP_OK,
                                    "text/html", document);
                        } else if ("/style.css".equals(path)) {
                            send(exchange, HttpURLConnection.HTTP_OK,
                                    "text/css", css);
                        } else if ("/app.js".equals(path)) {
                            send(exchange, HttpURLConnection.HTTP_OK,
                                    "application/javascript", js);
                        } else {
                            send(exchange, HttpURLConnection.HTTP_NOT_FOUND,
                                    "text/plain", "Not found");
                        }
                    } else {
                        send(exchange, HttpURLConnection.HTTP_BAD_METHOD,
                                "text/plain", "Method not allowed");
                    }
                } catch (IOException exception) {
                    System.err.println(
                            "Web/log I/O error: " + exception.getMessage());

                    try {
                        send(exchange,
                                HttpURLConnection.HTTP_INTERNAL_ERROR,
                                "text/plain",
                                "I/O failure; stop testing and check server logs.");
                    } catch (IOException ignored) {
                        /* Connection may already be closed. */
                    }

                    stopped.countDown();
                } finally {
                    exchange.close();
                }
            });

            Thread hook = new Thread(
                    stopped::countDown, "crobs-web-shutdown");
            Runtime.getRuntime().addShutdownHook(hook);

            try {
                server.start();
                System.out.println(
                        "CROBS: https://" + SERVER_HOST
                                + " (one shared local session)");
                stopped.await();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                server.stop(SHUTDOWN_DELAY_SECONDS);

                try {
                    Runtime.getRuntime().removeShutdownHook(hook);
                } catch (IllegalStateException ignored) {
                    /* JVM shutdown is already in progress. */
                }
            }
        } finally {
            server.stop(0);
        }
    }

    /**
     * Reads the request body as UTF-8, rejecting a payload larger than
     * {@value #MAX_COMMAND_BYTES} bytes.
     *
     * @param exchange the exchange providing the request body
     * @return the decoded body; nonblank and single-line checks
     *         are performed by the caller
     * @throws IOException if reading fails or the size limit is exceeded
     */
    private static String readCommand(HttpExchange exchange)
            throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (InputStream in = exchange.getRequestBody()) {
            byte[] buffer = new byte[READ_BUFFER_BYTES];
            int count;

            while ((count = in.read(buffer)) != -1) {
                if (bytes.size() + count > MAX_COMMAND_BYTES) {
                    throw new IOException(
                            "Command exceeds " + MAX_COMMAND_BYTES + " bytes");
                }

                bytes.write(buffer, 0, count);
            }
        }

        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Sends a UTF-8 response with no-store, content-type, and security headers.
     *
     * @param exchange the exchange receiving the response
     * @param status the HTTP response status
     * @param type the MIME type without a charset
     * @param body the non-null response text
     * @throws IOException if headers or response bytes cannot be written
     */
    private static void send(
            HttpExchange exchange, int status, String type, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type", type + "; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set(
                "X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set(
                "Content-Security-Policy",
                "default-src 'self'; script-src 'self'; style-src 'self'; "
                        + "frame-ancestors 'none'; form-action 'self'");

        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }
}
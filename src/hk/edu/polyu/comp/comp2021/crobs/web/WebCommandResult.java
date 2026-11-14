package hk.edu.polyu.comp.comp2021.crobs.web;

import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.model.User;

/** JSON envelope for all Web command results; CLI formatting remains unchanged. */
public final class WebCommandResult {
    /**
     * Prevents construction of this JSON formatting utility.
     */
    private WebCommandResult() { }

    /**
     * Builds a result without embedding executable HTML or exposing additional booking data.
     * @param command original command line
     * @param output existing command output
     * @param model model owning the shared session
     * @return a JSON object with ok false for Error-prefixed output, preserving
     *         the original error message in the output field
     * @throws NullPointerException if command, output, or model is null
     */
    public static String toJson(String command, String output, CROBS model) {
        User user = model.getCurrentUser();
        String verb = command.trim().split("\\s+", 2)[0];
        return "{\"command\":" + quote(verb)
                + ",\"ok\":" + !output.trim().startsWith("Error:")
                + ",\"output\":" + quote(output)
                + ",\"canUndo\":" + model.canUndo()
                + ",\"canRedo\":" + model.canRedo()
                + ",\"userId\":" + quote(user == null ? "" : user.getUserId())
                + ",\"userRole\":" + quote(user == null ? "" : user.getRole().name()) + "}";
    }

    /**
     * Quotes text as JSON, escaping controls and surrogate code units.
     *
     * @param value the non-null text to encode
     * @return the quoted JSON string literal
     * @throws NullPointerException if value is null
     */
    private static String quote(String value) {
        StringBuilder result = new StringBuilder("\"");
        for (int index = 0; index < value.length(); index++) {
            char ch = value.charAt(index);
            switch (ch) {
                case '"': result.append("\\\""); break;
                case '\\': result.append("\\\\"); break;
                case '\n': result.append("\\n"); break;
                case '\r': result.append("\\r"); break;
                case '\t': result.append("\\t"); break;
                default:
                    if (ch < 0x20 || Character.isSurrogate(ch)) {
                        String hex = Integer.toHexString(ch);
                        result.append("\\u");
                        for (int pad = hex.length(); pad < 4; pad++) result.append('0');
                        result.append(hex);
                    } else result.append(ch);
            }
        }
        return result.append('"').toString();
    }
}

package dansplugins.netheraccesscontroller.utils;

/**
 * @author Daniel McCoy Stephenson
 */
public class ArgumentParser {

    /**
     * Returns the text between the first and the last single quote in the arguments, rejoined with
     * single spaces, so an apostrophe inside the text is kept. Returns null if there are fewer than
     * two single quotes.
     */
    public String getTextInsideOuterSingleQuotes(String[] args) {
        String argumentString = String.join(" ", args);

        int start = argumentString.indexOf('\'');
        int end = argumentString.lastIndexOf('\'');
        if (start == -1 || end == start) {
            return null;
        }

        return argumentString.substring(start + 1, end);
    }

}

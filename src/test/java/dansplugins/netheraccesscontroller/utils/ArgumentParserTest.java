package dansplugins.netheraccesscontroller.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Covers {@link ArgumentParser#getTextInsideOuterSingleQuotes}, which is how ConfigCommand reads a
 * deny message that contains spaces. Bukkit hands the command over already split on spaces, so the
 * parser rejoins the arguments with single spaces and returns the text between the first and the
 * last single quote. Fewer than two quotes yields null.
 */
class ArgumentParserTest {

    private final ArgumentParser argumentParser = new ArgumentParser();

    @Test
    void noQuotes_returnsNull() {
        assertNull(parse("set", "denyUsageMessage", "hello"));
    }

    @Test
    void quotedWords_areRejoinedWithSingleSpaces() {
        assertEquals("You cannot enter the nether.",
                parse("set", "denyUsageMessage", "'You", "cannot", "enter", "the", "nether.'"));
    }

    @Test
    void singleQuotedWord_isReturnedWithoutItsQuotes() {
        assertEquals("Denied.", parse("'Denied.'"));
    }

    @Test
    void emptyQuotes_returnAnEmptyString() {
        assertEquals("", parse("''"));
    }

    @Test
    void unpairedQuote_returnsNull() {
        assertNull(parse("'never", "closed"));
    }

    @Test
    void apostropheInsideQuotes_isKept() {
        assertEquals("You don't pass.", parse("'You", "don't", "pass.'"));
    }

    @Test
    void severalApostrophesInsideQuotes_areKept() {
        assertEquals("Don't. Won't. Can't.", parse("'Don't.", "Won't.", "Can't.'"));
    }

    @Test
    void textOutsideTheOuterQuotes_isDropped() {
        assertEquals("inside", parse("set", "denyUsageMessage", "'inside'", "outside"));
    }

    private String parse(String... args) {
        return argumentParser.getTextInsideOuterSingleQuotes(args);
    }
}

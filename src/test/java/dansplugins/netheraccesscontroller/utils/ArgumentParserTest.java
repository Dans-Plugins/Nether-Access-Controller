package dansplugins.netheraccesscontroller.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Covers {@link ArgumentParser#getArgumentsInsideSingleQuotes}, which is how ConfigCommand reads a
 * deny message that contains spaces. Bukkit hands the command over already split on spaces, so the
 * parser rejoins the arguments with single spaces and returns the text between each pair of single
 * quotes, in order. An unpaired quote is ignored.
 */
class ArgumentParserTest {

    private final ArgumentParser argumentParser = new ArgumentParser();

    @Test
    void noQuotes_returnsNothing() {
        assertEquals(Collections.emptyList(), parse("set", "denyUsageMessage", "hello"));
    }

    @Test
    void quotedWords_areRejoinedWithSingleSpaces() {
        assertEquals(Collections.singletonList("You cannot enter the nether."),
                parse("set", "denyUsageMessage", "'You", "cannot", "enter", "the", "nether.'"));
    }

    @Test
    void singleQuotedWord_isReturnedWithoutItsQuotes() {
        assertEquals(Collections.singletonList("Denied."), parse("'Denied.'"));
    }

    @Test
    void emptyQuotes_returnAnEmptyString() {
        assertEquals(Collections.singletonList(""), parse("''"));
    }

    @Test
    void severalQuotedSections_areReturnedInOrder() {
        assertEquals(Arrays.asList("first one", "second"), parse("'first", "one'", "and", "'second'"));
    }

    @Test
    void unpairedQuote_isIgnored() {
        assertEquals(Collections.emptyList(), parse("'never", "closed"));
    }

    @Test
    void trailingUnpairedQuote_isIgnoredAfterACompletePair() {
        assertEquals(Collections.singletonList("closed"), parse("'closed'", "'open"));
    }

    /**
     * Characterizes current behavior: an apostrophe inside the message is read as the closing
     * quote, so the message is cut short at it. Tracked as a bug in issue #60; this test pins what
     * the parser does today rather than what it should do.
     */
    @Test
    void apostropheInsideQuotes_endsTheQuotedSection() {
        assertEquals(Collections.singletonList("You don"), parse("'You", "don't", "pass.'"));
    }

    private List<String> parse(String... args) {
        return argumentParser.getArgumentsInsideSingleQuotes(args);
    }
}

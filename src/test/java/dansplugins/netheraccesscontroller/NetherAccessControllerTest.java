package dansplugins.netheraccesscontroller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the version comparison that onEnable uses to decide whether an existing config file
 * needs its defaults written back. The comparison runs before anything else in onEnable, so an
 * exception thrown here stops the plugin from enabling, and a plugin that does not enable
 * registers no listeners — every player can then create and use nether portals regardless of the
 * whitelist.
 *
 * The comparison is exercised through its static form; the instance form only supplies it with
 * the config value and the running version, both of which need a live Bukkit server to obtain.
 */
class NetherAccessControllerTest {

    @Test
    void isVersionMismatched_withMatchingVersions_reportsNoMismatch() {
        assertFalse(NetherAccessController.isVersionMismatched("v2.0.0", "v2.0.0"));
    }

    @Test
    void isVersionMismatched_ignoresCase() {
        assertFalse(NetherAccessController.isVersionMismatched("V2.0.0", "v2.0.0"));
    }

    @Test
    void isVersionMismatched_withDifferentVersions_reportsMismatch() {
        assertTrue(NetherAccessController.isVersionMismatched("v1.1.0", "v2.0.0"));
    }

    /**
     * A config file with no version key at all must be reported as mismatched rather than
     * dereferenced, so that the caller rewrites the defaults instead of aborting startup.
     */
    @Test
    void isVersionMismatched_withNoVersionRecorded_reportsMismatch() {
        assertTrue(NetherAccessController.isVersionMismatched(null, "v2.0.0"));
    }
}

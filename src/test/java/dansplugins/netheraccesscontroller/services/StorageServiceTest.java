package dansplugins.netheraccesscontroller.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the save-file recovery behaviour of {@link StorageService#loadDataFromFilename}, which
 * runs from onEnable. An exception thrown from here stops the plugin from enabling, and a plugin
 * that does not enable registers no listeners, so every player can create and use nether portals
 * regardless of the whitelist. Every unreadable input is therefore asserted to yield an empty
 * list instead, which keeps the plugin running with an empty (deny-everyone) whitelist.
 *
 * The collaborators are null because loadDataFromFilename reads none of them. The methods that do
 * require a live Bukkit server are out of scope here and are covered by manual validation instead.
 */
class StorageServiceTest {

    @TempDir
    File temporaryDirectory;

    private final StorageService storageService = new StorageService(null, null, null);

    @Test
    void loadDataFromFilename_withNoFile_returnsEmptyList() {
        File saveFile = new File(temporaryDirectory, "allowedPlayers.json");

        ArrayList<HashMap<String, String>> data = storageService.loadDataFromFilename(saveFile.getPath());

        assertTrue(data.isEmpty());
        assertFalse(unreadableCopyOf(saveFile).exists(),
                "A save file that was never written is normal on first run and must not be renamed");
    }

    @Test
    void loadDataFromFilename_withValidFile_returnsSavedData() throws IOException {
        File saveFile = saveFileContaining("[{\"allowedPlayers\":\"[]\"}]");

        ArrayList<HashMap<String, String>> data = storageService.loadDataFromFilename(saveFile.getPath());

        assertEquals(1, data.size());
        assertEquals("[]", data.get(0).get("allowedPlayers"));
        assertTrue(saveFile.exists());
        assertFalse(unreadableCopyOf(saveFile).exists());
    }

    @Test
    void loadDataFromFilename_withEmptyFile_returnsEmptyListAndSetsTheFileAside() throws IOException {
        File saveFile = saveFileContaining("");

        ArrayList<HashMap<String, String>> data = storageService.loadDataFromFilename(saveFile.getPath());

        assertTrue(data.isEmpty());
        assertRenamedOutOfTheWay(saveFile);
    }

    @Test
    void loadDataFromFilename_withTruncatedFile_returnsEmptyListAndSetsTheFileAside() throws IOException {
        // What a crash part-way through a save leaves behind.
        File saveFile = saveFileContaining("[{\"allowedPlayers\":\"[\\\"c0ffee\"");

        ArrayList<HashMap<String, String>> data = storageService.loadDataFromFilename(saveFile.getPath());

        assertTrue(data.isEmpty());
        assertRenamedOutOfTheWay(saveFile);
    }

    @Test
    void loadDataFromFilename_withUnexpectedJsonShape_returnsEmptyListAndSetsTheFileAside() throws IOException {
        File saveFile = saveFileContaining("{\"allowedPlayers\":\"[]\"}");

        ArrayList<HashMap<String, String>> data = storageService.loadDataFromFilename(saveFile.getPath());

        assertTrue(data.isEmpty());
        assertRenamedOutOfTheWay(saveFile);
    }

    /**
     * The rename is what keeps the save on shutdown from overwriting a file an operator could
     * still repair by hand.
     */
    private void assertRenamedOutOfTheWay(File saveFile) {
        assertFalse(saveFile.exists(), "The unreadable save file should no longer be in place");
        assertTrue(unreadableCopyOf(saveFile).exists(),
                "The unreadable save file should have been kept under its renamed path");
    }

    private File unreadableCopyOf(File saveFile) {
        return new File(saveFile.getPath() + ".unreadable");
    }

    private File saveFileContaining(String contents) throws IOException {
        File saveFile = new File(temporaryDirectory, "allowedPlayers.json");
        Files.write(saveFile.toPath(), contents.getBytes(StandardCharsets.UTF_8));
        return saveFile;
    }
}

package dansplugins.netheraccesscontroller.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import dansplugins.netheraccesscontroller.NetherAccessController;
import dansplugins.netheraccesscontroller.data.PersistentData;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel McCoy Stephenson
 */
public class StorageService {
    private final ConfigService configService;
    private final NetherAccessController netherAccessController;
    private final PersistentData persistentData;

    private final static String FILE_PATH = "./plugins/NetherAccessController/";
    private final static String ALLOWED_PLAYERS_FILE_NAME = "allowedPlayers.json";
    private final static String UNREADABLE_FILE_SUFFIX = ".unreadable";

    /**
     * How many destinations are tried before an unreadable save file is left where it is. A bound
     * is needed because each attempt touches the filesystem, and an operator who has accumulated
     * this many unread copies is not acting on the log messages anyway.
     *
     * Package-private so that the exhausted case can be set up by tests without restating the bound.
     */
    final static int MAXIMUM_UNREADABLE_FILE_COPIES = 100;

    private final static Type LIST_MAP_TYPE = new TypeToken<ArrayList<HashMap<String, String>>>(){}.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();;

    public StorageService(ConfigService configService, NetherAccessController netherAccessController, PersistentData persistentData) {
        this.configService = configService;
        this.netherAccessController = netherAccessController;
        this.persistentData = persistentData;
    }

    public void save() {
        saveAllowedPlayers();
        if (configService.hasBeenAltered()) {
            netherAccessController.saveConfig();
        }
    }

    public void load() {
        loadAllowedPlayers();
    }

    private void saveAllowedPlayers() {
        List<Map<String, String>> allowedPlayers = new ArrayList<>();
        allowedPlayers.add(persistentData.save());

        writeOutFiles(allowedPlayers);
    }

    private void writeOutFiles(List<Map<String, String>> saveData) {
        try {
            File parentFolder = new File(FILE_PATH);
            parentFolder.mkdir();
            File file = new File(FILE_PATH + ALLOWED_PLAYERS_FILE_NAME);
            file.createNewFile();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            outputStreamWriter.write(gson.toJson(saveData));
            outputStreamWriter.close();
        } catch(IOException e) {
            System.out.println("ERROR: " + e.toString());
        }
    }

    private void loadAllowedPlayers() {

        // persistentData.clear();

        ArrayList<HashMap<String, String>> data = loadDataFromFilename(FILE_PATH + ALLOWED_PLAYERS_FILE_NAME);

        if (data.size() > 0 && data.get(0) != null) {
            persistentData.load(data.get(0));
        }
        else {
            if (netherAccessController.isDebugEnabled()) {
                // Reached when no save file exists yet, and also when one exists but nothing could
                // be read from it, so the message does not claim the file is absent.
                System.out.println("[DEBUG] No whitelist was loaded from a save file!");
            }
        }

    }

    /**
     * Reads the save file at the given path, returning an empty list when nothing can be read
     * from it.
     *
     * An unreadable save file must not stop the plugin from enabling: a plugin that fails to
     * enable registers no listeners, so every player can create and use nether portals. An empty
     * list keeps the plugin running with an empty whitelist, which denies everyone instead.
     *
     * A file that exists but cannot be parsed is renamed out of the way first, because the save
     * on shutdown would otherwise overwrite it and destroy whatever could still be recovered
     * from it by hand.
     *
     * Package-private so that the recovery behaviour can be exercised directly by tests.
     */
    ArrayList<HashMap<String, String>> loadDataFromFilename(String filename) {
        File file = new File(filename);

        if (!file.exists()) {
            // Fail silently because this can actually happen in normal use
            return new ArrayList<>();
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            ArrayList<HashMap<String, String>> data = gson.fromJson(new JsonReader(reader), LIST_MAP_TYPE);
            if (data != null) {
                return data;
            }
            // Gson returns null for an empty document.
            System.out.println("ERROR: " + filename + " is empty.");
        } catch (JsonParseException e) {
            System.out.println("ERROR: " + filename + " could not be parsed: " + e.toString());
        } catch (IOException e) {
            System.out.println("ERROR: " + e.toString());
            return new ArrayList<>();
        }

        setUnreadableFileAside(file);
        return new ArrayList<>();
    }

    /**
     * Renames an unreadable save file out of the way, onto the first destination not already
     * taken by one set aside on an earlier occasion.
     *
     * A file set aside earlier is itself still worth recovering by hand, so it is stepped over
     * rather than replaced: the destination is numbered upwards until a free one is found, and
     * the rename is given up on rather than performed if none is.
     */
    private void setUnreadableFileAside(File file) {
        for (int attempt = 1; attempt <= MAXIMUM_UNREADABLE_FILE_COPIES; attempt++) {
            Path destination = unreadableDestinationFor(file, attempt);
            try {
                // REPLACE_EXISTING is deliberately not passed. Files.move then fails on a taken
                // destination instead of silently replacing what is there, which File.renameTo
                // does on a POSIX filesystem, and it does so without the race a prior exists()
                // check would leave open.
                Files.move(file.toPath(), destination);
                System.out.println("ERROR: " + file.getPath() + " has been renamed to " + destination
                        + " so that it is not overwritten. No player is allowed to access the nether until the"
                        + " whitelist is rebuilt or the file is repaired and restored.");
                return;
            } catch (FileAlreadyExistsException e) {
                // Taken by a save file set aside earlier, so the next destination is tried.
            } catch (IOException e) {
                System.out.println("ERROR: " + file.getPath() + " could not be renamed to " + destination
                        + ": " + e.toString());
                break;
            }
        }

        System.out.println("ERROR: " + file.getPath() + " could not be set aside and will be overwritten"
                + " when the server stops. A copy should be taken now if its contents are still wanted."
                + " No player is allowed to access the nether in the meantime.");
    }

    /**
     * The destination for the given attempt: the plain suffix first, then the same suffix numbered
     * upwards. The first attempt is left unnumbered so that the common case — nothing set aside
     * before — keeps the path the previous behaviour used and the user guide documents.
     */
    private Path unreadableDestinationFor(File file, int attempt) {
        if (attempt == 1) {
            return Paths.get(file.getPath() + UNREADABLE_FILE_SUFFIX);
        }
        return Paths.get(file.getPath() + UNREADABLE_FILE_SUFFIX + "." + attempt);
    }

}

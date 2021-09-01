package dansplugins.netheraccesscontroller.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import dansplugins.netheraccesscontroller.NetherAccessController;
import dansplugins.netheraccesscontroller.PersistentData;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class StorageManager {

    private static StorageManager instance;

    private final static String FILE_PATH = "./plugins/NetherAccessController/";
    private final static String ALLOWED_PLAYERS_FILE_NAME = "allowedPlayers.json";

    private final static Type LIST_MAP_TYPE = new TypeToken<ArrayList<HashMap<String, String>>>(){}.getType();

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();;

    private StorageManager() {

    }

    public static StorageManager getInstance() {
        if (instance == null) {
            instance = new StorageManager();
        }
        return instance;
    }

    public void save() {
        saveAllowedPlayers();
        if (ConfigManager.getInstance().hasBeenAltered()) {
            NetherAccessController.getInstance().saveConfig();
        }
    }

    public void load() {
        loadAllowedPlayers();
    }

    private void saveAllowedPlayers() {
        List<Map<String, String>> allowedPlayers = new ArrayList<>();
        allowedPlayers.add(PersistentData.getInstance().save());

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

        // PersistentData.getInstance().clear();

        ArrayList<HashMap<String, String>> data = loadDataFromFilename(FILE_PATH + ALLOWED_PLAYERS_FILE_NAME);

        if (data.size() > 0) {
            PersistentData.getInstance().load(data.get(0));
        }
        else {
            if (NetherAccessController.getInstance().isDebugEnabled()) {
                System.out.println("[DEBUG] No save files found!");
            }
        }

    }

    private ArrayList<HashMap<String, String>> loadDataFromFilename(String filename) {
        try{
            Gson gson = new GsonBuilder().setPrettyPrinting().create();;
            JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));
            return gson.fromJson(reader, LIST_MAP_TYPE);
        } catch (FileNotFoundException e) {
            // Fail silently because this can actually happen in normal use
        }
        return new ArrayList<>();
    }

}

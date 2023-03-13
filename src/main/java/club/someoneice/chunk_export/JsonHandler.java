package club.someoneice.chunk_export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonHandler {
    public static List<JsonBlockOutputInfo> infoList = new ArrayList<>();
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public JsonHandler(@Nullable String dimId) {
        try {
            dimId = dimId == null ? "" : "_" + dimId;
            File file = new File(System.getProperty("user.dir") + File.separator + "export", "world_gem" + dimId + ".json");
            if (!file.getParentFile().isDirectory()) file.getParentFile().mkdirs();
            if (!file.isFile() || !file.exists()) file.createNewFile();

            String str = gson.toJson(infoList);
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(str.getBytes());
            stream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public JsonHandler() {
        this(null);
    }

    public static class JsonBlockOutputInfo {
        String ore_name;
        Map<Integer, Double> ore_distrib;
        boolean silktouch;
        Map<String, Integer> dropsList;

        public JsonBlockOutputInfo(String ore_name, Map<Integer, Double> ore_distrib, boolean silktouch, Map<String, Integer> dropsList) {
            this.ore_name = ore_name;
            this.ore_distrib = ore_distrib;
            this.silktouch = silktouch;
            this.dropsList = dropsList;
        }
    }
}

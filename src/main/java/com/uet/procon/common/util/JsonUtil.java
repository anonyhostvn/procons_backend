package com.uet.procon.common.util;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.uet.procon.common.model.GameInfo;
import com.uet.procon.common.model.MapModel;
import org.slf4j.Logger;

import java.io.FileReader;

public class JsonUtil {

    private Logger logger = LoggingUtil.createLogger(JsonUtil.class);

    public static GameInfo[] readGameInfosFromFile() throws Exception {
        String file = JsonUtil.class.getClassLoader().getResource("procon-fields/game-info.json").getFile();
        JsonReader jsonReader = new JsonReader(new FileReader(file));
        return new Gson().fromJson(jsonReader, GameInfo[].class);
    }

    public static MapModel readMapFromFile(int mapId) throws Exception {
        String file = JsonUtil.class.getClassLoader().getResource("procon-fields/" + mapId + ".json").getFile();
        JsonReader jsonReader = new JsonReader(new FileReader(file));
        return new Gson().fromJson(jsonReader, MapModel.class);
    }
}

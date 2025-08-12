package database;

import com.google.gson.Gson;
import model.Laberinto;

public class LaberintoSerializer {
    private static final Gson gson = new Gson();

    public static String toJson(Laberinto laberinto) {
        return gson.toJson(laberinto);
    }

    public static Laberinto fromJson(String json) {
        return gson.fromJson(json, Laberinto.class);
    }
}

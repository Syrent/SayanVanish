package ir.syrent.velocityvanish.velocity.vruom.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

public class GsonUtils {

    private final static Gson GSON = new GsonBuilder().create();
    private final static Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();
    private final static JsonParser JSON_PARSER = new JsonParser();

    public static Gson get() {
        return GSON;
    }

    public static Gson getPrettyPrinter() {
        return GSON_PRETTY;
    }

    public static JsonParser getParser() {
        return JSON_PARSER;
    }

}
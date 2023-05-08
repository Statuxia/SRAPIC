package me.statuxia.srapic.config;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
    private final Path path;

    @Getter
    private JSONObject jsonObject;

    @Getter
    private boolean isCreated = false;

    public ConfigManager(@NotNull String path) throws IOException {
        this.path = getPath(path);
        create();
        this.jsonObject = getObject();
    }

    public static @NotNull ConfigManager of(@NotNull String path) throws IOException {
        return new ConfigManager(path);
    }

    private @NotNull Path getPath(@NotNull String stringPath) {
        return Paths.get(stringPath);
    }

    private void create() throws IOException {
        if (!Files.isDirectory(path.getParent())) {
            Files.createDirectories(path.getParent());
            isCreated = true;
        }
        if (Files.notExists(path)) {
            Files.createFile(path);
            isCreated = true;
        }
    }

    public @NotNull ConfigManager updateFile() throws IOException {
        Files.writeString(path, this.jsonObject.toString(), StandardCharsets.UTF_8);
        return this;
    }

    public @NotNull ConfigManager updateFile(@NotNull JSONObject jsonObject, boolean saveInObject) throws IOException {
        if (saveInObject) {
            this.jsonObject = jsonObject;
        }
        Files.writeString(path, jsonObject.toString(), StandardCharsets.UTF_8);
        return this;
    }

    public @NotNull JSONObject getObject() throws IOException {
        create();
        try {
            return new JSONObject(Files.readString(path, StandardCharsets.UTF_8));
        } catch (JSONException e) {
            return new JSONObject();
        }
    }
}

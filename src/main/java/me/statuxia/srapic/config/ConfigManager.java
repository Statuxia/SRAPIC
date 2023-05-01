package me.statuxia.srapic.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
    @Setter(AccessLevel.PRIVATE)
    private Path path;

    @Getter
    private JSONObject jsonObject;

    @Getter
    private boolean isCreated = false;

    /**
     * Конструктор класса, заполняющийся объектом <code>Path</code> с путем до файла
     * и объектом <code>JSONObject</code> с данными из файла.
     *
     * @param path путь до json файла, который необходимо открыть.
     */
    public ConfigManager(@NotNull String path) throws IOException {
        this.path = getPath(path);
        create();
        this.jsonObject = getObject();
    }

    /**
     * Cтатический метод для создания объекта класса <code>JSONManager</code>.
     *
     * @param path путь до json файла, который необходимо открыть.
     */
    public static @NotNull ConfigManager of(@NotNull String path) throws IOException {
        return new ConfigManager(path);
    }

    /**
     * Получает путь до файла в виде строки и возвращает в виде объекта <code>Path</code>.
     *
     * @param stringPath путь до файла в виде строки.
     * @return путь до файла
     * @see Path
     */
    private @NotNull Path getPath(@NotNull String stringPath) {
        return Paths.get(stringPath);
    }

    /**
     * Создает директории и сам файл по пути в объекте, если они отсутствуют.
     */
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

    /**
     * Позволяет обновить файл, используя вложенный объект <code>JSONObject</code>.
     *
     * @return тот же объект используемого класса.
     */
    public @NotNull ConfigManager updateFile() throws IOException {
        Files.writeString(path, this.jsonObject.toString(), StandardCharsets.UTF_8);
        return this;
    }

    /**
     * Позволяет обновить файл используя передаваемый объект <code>JSONObject</code>, который также можно
     * сохранить внутри объекта <code>JSONManager</code>, передав <code>boolean</code> аргумент для сохранения.
     *
     * @param jsonObject   объект класса <code>jsonObject</code>, который требуется сохранить в файл.
     * @param saveInObject <code>boolean</code> тип данных, при <code>true</code> сохраняет передаваемые данные в объект.
     * @return тот же объект используемого класса.
     */
    public @NotNull ConfigManager updateFile(@NotNull JSONObject jsonObject, boolean saveInObject) throws IOException {
        if (saveInObject) {
            this.jsonObject = jsonObject;
        }
        Files.writeString(path, jsonObject.toString(), StandardCharsets.UTF_8);
        return this;
    }

    /**
     * Позволяет получить объект <code>JSONObject</code>, заполненный данными из файла, который
     * находится по пути из конструктора объекта.
     * В случае, если файл или директории до файла были удалены, они будут созданы вновь.
     *
     * @return объект <code>JSONObject</code>.
     */
    public @NotNull JSONObject getObject() throws IOException {
        create();
        try {
            return new JSONObject(Files.readString(path, StandardCharsets.UTF_8));
        } catch (JSONException e) {
            return new JSONObject();
        }
    }
}

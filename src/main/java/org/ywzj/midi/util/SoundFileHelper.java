package org.ywzj.midi.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.ywzj.midi.YwzjMidi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class SoundFileHelper {

    public static final String SOUNDS_JSON = "sounds.json";
    public static final String SOUND_ROOT = "assets/%s/sounds/";
    public static final String OGG_SUFFIX = ".ogg";

    private SoundFileHelper() {}

    public static Map<String, byte[]> scanVirtualSoundsJson(Path rootPath, String namespace) {
        LinkedHashMap<ResourceLocation, List<String>> soundMap = new LinkedHashMap<>();
        if (Files.isDirectory(rootPath)) {
            scanDirectory(rootPath, namespace, soundMap);
        } else {
            scanZip(rootPath, namespace, soundMap);
        }
        return buildVirtualSoundsJson(soundMap);
    }

    private static void scanDirectory(Path rootPath, String namespace, Map<ResourceLocation, List<String>> soundMap) {
        Path assetsRoot = rootPath.resolve("assets");
        if (!Files.isDirectory(assetsRoot)) {
            return;
        }
        Path namespacePath = assetsRoot.resolve(namespace);
        if (!Files.isDirectory(namespacePath)) {
            return;
        }
        try (DirectoryStream<Path> ignored = Files.newDirectoryStream(namespacePath)) {
            collectFromDirectory(rootPath, namespace, String.format(SOUND_ROOT, namespace), soundMap);
        } catch (IOException e) {
            YwzjMidi.LOGGER.warn("Failed to scan auto sounds from {}", rootPath, e);
        }
    }

    private static void collectFromDirectory(Path rootPath, String namespace, String prefix,
                                             Map<ResourceLocation, List<String>> soundMap) throws IOException {
        Path scanRoot = rootPath.resolve(prefix);
        if (!Files.isDirectory(scanRoot)) {
            return;
        }
        try (var stream = Files.walk(scanRoot)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(OGG_SUFFIX))
                    .forEach(path -> addSoundEntry(namespace, toUnixPath(rootPath.relativize(path)), soundMap));
        }
    }

    private static void scanZip(Path zipPath, String namespace, Map<ResourceLocation, List<String>> soundMap) {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            var entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(OGG_SUFFIX)) {
                    continue;
                }
                String path = entry.getName();
                String prefix = String.format(SOUND_ROOT, namespace);
                if (!path.startsWith(prefix)) {
                    continue;
                }
                addSoundEntry(namespace, path, soundMap);
            }
        } catch (IOException e) {
            YwzjMidi.LOGGER.warn("Failed to scan auto sounds from {}", zipPath, e);
        }
    }

    private static void addSoundEntry(String namespace, String fullPath, Map<ResourceLocation, List<String>> soundMap) {
        String prefix = String.format(SOUND_ROOT, namespace);
        if (!fullPath.startsWith(prefix)) {
            return;
        }
        String relativePath = fullPath.substring(prefix.length(), fullPath.length() - OGG_SUFFIX.length());
        String soundName = toSoundName(relativePath);
        if (soundName == null) {
            YwzjMidi.LOGGER.warn("Skip auto-registering sound {} because path does not match midi convention", fullPath);
            return;
        }
        ResourceLocation soundId = YwzjMidi.resourceLocation(namespace, soundName);
        String soundEntry = namespace + ":" + relativePath;
        soundMap.computeIfAbsent(soundId, key -> new ArrayList<>()).add(soundEntry);
    }

    @Nullable
    private static String toSoundName(String relativePath) {
        String[] segments = relativePath.split("/");
        if (segments.length == 2) {
            return segments[0] + "_" + segments[1];
        }
        if (segments.length == 3) {
            return segments[0] + "_" + segments[2] + "_" + segments[1];
        }
        return null;
    }

    public static Map<String, byte[]> buildVirtualSoundsJson(Map<ResourceLocation, List<String>> soundMap) {
        LinkedHashMap<String, JsonObject> jsonByNamespace = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, List<String>> entry : soundMap.entrySet()) {
            ResourceLocation soundId = entry.getKey();
            JsonObject soundsJson = jsonByNamespace.computeIfAbsent(soundId.getNamespace(), key -> new JsonObject());
            JsonObject definition = new JsonObject();
            JsonArray sounds = new JsonArray();
            for (String soundEntry : entry.getValue()) {
                sounds.add(soundEntry);
            }
            definition.add("sounds", sounds);
            soundsJson.add(soundId.getPath(), definition);
        }

        LinkedHashMap<String, byte[]> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonObject> entry : jsonByNamespace.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toString().getBytes(StandardCharsets.UTF_8));
        }
        return Map.copyOf(result);
    }

    public static boolean isVirtualSoundsJson(ResourceLocation location) {
        return SOUNDS_JSON.equals(location.getPath());
    }

    public static InputStream mergeSoundsJson(InputStream original, byte[] generatedBytes) throws IOException {
        JsonObject generated = com.google.gson.JsonParser.parseReader(new InputStreamReader(
                new ByteArrayInputStream(generatedBytes), StandardCharsets.UTF_8)).getAsJsonObject();
        JsonObject merged;
        try (InputStream input = original) {
            JsonElement originalJson = com.google.gson.JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            merged = originalJson != null && originalJson.isJsonObject() ? originalJson.getAsJsonObject() : new JsonObject();
        }
        for (Map.Entry<String, JsonElement> entry : generated.entrySet()) {
            if (!merged.has(entry.getKey())) {
                merged.add(entry.getKey(), entry.getValue());
                continue;
            }
            JsonObject existing = merged.getAsJsonObject(entry.getKey());
            JsonObject addition = entry.getValue().getAsJsonObject();
            JsonArray existingSounds = existing.has("sounds") && existing.get("sounds").isJsonArray()
                    ? existing.getAsJsonArray("sounds")
                    : new JsonArray();
            Set<String> present = new LinkedHashSet<>();
            for (JsonElement sound : existingSounds) {
                if (sound.isJsonPrimitive()) {
                    present.add(sound.getAsString());
                }
            }
            if (addition.has("sounds") && addition.get("sounds").isJsonArray()) {
                for (JsonElement sound : addition.getAsJsonArray("sounds")) {
                    if (sound.isJsonPrimitive() && present.add(sound.getAsString())) {
                        existingSounds.add(sound.getAsString());
                    }
                }
            }
            existing.add("sounds", existingSounds);
        }
        return new ByteArrayInputStream(merged.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String toUnixPath(Path path) {
        return path.toString().replace('\\', '/');
    }

}

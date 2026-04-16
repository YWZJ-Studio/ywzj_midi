package org.ywzj.midi.resource;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.resource.DelegatingPackResources;
import net.minecraftforge.resource.PathPackResources;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.jetbrains.annotations.NotNull;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.custom.serialize.GsonUtil;
import org.ywzj.midi.util.GetJarResources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public enum InstrumentPackLoader implements RepositorySource {

    INSTANCE;
    private static final Marker MARKER = MarkerManager.getMarker("InstrumentPackFinder");
    public PackType packType;
    private static final Path INSTRUMENT_PACKS_PATH = FMLPaths.GAMEDIR.get().resolve("limitless_instrument");
    private static final Path INSTRUMENT_PACKS_BACKUP_PATH = FMLPaths.GAMEDIR.get().resolve("limitless_instrument_backup");
    private static final String DEFAULT_INSTRUMENT_PACK_PATH = "default_instrument";
    private List<InstrumentPack> instrumentPacks;
    static {
        try {
            File folder = INSTRUMENT_PACKS_PATH.toFile();
            if (!folder.isDirectory()) {
                Files.createDirectories(folder.toPath());
            }
            folder = INSTRUMENT_PACKS_BACKUP_PATH.toFile();
            if (!folder.isDirectory()) {
                Files.createDirectories(folder.toPath());
            }
        } catch (Exception e) {
            YwzjMidi.LOGGER.warn(MARKER, "Failed to init instrument resource directory...", e);
        }
    }

    @Override
    public void loadPacks(@NotNull Consumer<Pack> pOnLoad) {
        for (Pack pack : instrumentPacksAsResource()) {
            pOnLoad.accept(pack);
        }
    }

    public void scanInstrumentPacks() {
        checkDefaultInstrumentPack();
        YwzjMidi.LOGGER.info(MARKER, "Start scanning for instrument packs in {}", INSTRUMENT_PACKS_PATH);
        instrumentPacks = scanInstrumentPacks(INSTRUMENT_PACKS_PATH);
        YwzjMidi.LOGGER.info(MARKER, "Found {} possible instrument pack(s)", instrumentPacks.size());
    }

    private void checkDefaultInstrumentPack() {
        Path defaultInstrumentPackPath = Path.of(INSTRUMENT_PACKS_PATH + "/" + DEFAULT_INSTRUMENT_PACK_PATH);
        if (Files.isDirectory(defaultInstrumentPackPath)) {
            try (InputStream streamExist = Files.newInputStream(defaultInstrumentPackPath.resolve("instrument_pack.meta.json"))) {
                PackMeta packMetaExist = GsonUtil.GSON.fromJson(new InputStreamReader(streamExist, StandardCharsets.UTF_8), PackMeta.class);
                if (packMetaExist != null) {
                    String versionExist = packMetaExist.getVersion();
                    try (InputStream streamJar = GetJarResources.readModFile("/" + DEFAULT_INSTRUMENT_PACK_PATH + "/instrument_pack.meta.json")) {
                        PackMeta packMetaJar = GsonUtil.GSON.fromJson(new InputStreamReader(streamJar, StandardCharsets.UTF_8), PackMeta.class);
                        if (packMetaJar != null) {
                            String versionJar = packMetaJar.getVersion();
                            if (versionExist.compareTo(versionJar) >= 0) {
                                return;
                            }
                        }
                    }
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                String timestamp = LocalDateTime.now().format(formatter);
                GetJarResources.copyFolder(defaultInstrumentPackPath.toUri(), INSTRUMENT_PACKS_BACKUP_PATH.resolve(DEFAULT_INSTRUMENT_PACK_PATH + "_" + timestamp));
                GetJarResources.deleteFiles(defaultInstrumentPackPath);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        GetJarResources.copyModDirectory("/" + DEFAULT_INSTRUMENT_PACK_PATH, defaultInstrumentPackPath);
    }

    private List<Pack> instrumentPacksAsResource() {
        List<Pack> packs = new ArrayList<>();
        List<PathPackResources> extensionPacks = new ArrayList<>();
        for (InstrumentPack instrumentPack : instrumentPacks) {
            PathPackResources packResources = new InstrumentPackResources(instrumentPack);
            extensionPacks.add(packResources);
            Pack pack = Pack.readMetaAndCreate("ywzj_midi_resources_" + instrumentPack.meta().getNamespace(),
                    Component.translatable(instrumentPack.meta().getTitle()),
                    true,
                    (id) -> new DelegatingPackResources(id,
                            false,
                            new PackMetadataSection(Component.translatable(instrumentPack.meta().getDescription()), SharedConstants.getCurrentVersion().getPackVersion(packType)), extensionPacks) {
                                public IoSupplier<InputStream> getRootResource(String... paths) {
                                    if (paths.length == 1 && paths[0].equals("pack.png")) {
                                        return packResources.getRootResource("pack.png");
                                    }
                                    return null;
                                }
                            }, packType, Pack.Position.BOTTOM, PackSource.BUILT_IN);
            packs.add(pack);
        }
        return packs;
    }

    private static InstrumentPack fromDirPath(Path path) throws IOException {
        Path packMetaPath = path.resolve("instrument_pack.meta.json");
        try (InputStream stream = Files.newInputStream(packMetaPath)) {
            PackMeta packMeta = GsonUtil.GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), PackMeta.class);

            if (packMeta == null) {
                YwzjMidi.LOGGER.warn(MARKER, "Failed to read packMeta json: {}", path.getFileName());
                return null;
            }

            if (packMeta.getNamespace() == null) {
                YwzjMidi.LOGGER.warn(MARKER, "Failed to read namespace: {}", path.getFileName());
                return null;
            }

            if (packMeta.getDependencies() != null && !modVersionAllMatch(packMeta)) {
                YwzjMidi.LOGGER.warn(MARKER, "Mod version mismatch: {}", path.getFileName());
                return null;
            }

            return new InstrumentPack(path, packMeta);
        } catch (IOException | JsonSyntaxException | JsonIOException | InvalidVersionSpecificationException exception) {
            YwzjMidi.LOGGER.warn(MARKER, "Failed to read info json: {}", path.getFileName());
            YwzjMidi.LOGGER.warn(exception.getMessage());
        }
        return null;
    }

    private static InstrumentPack fromZipPath(Path path)  {
        try(ZipFile zipFile = new ZipFile(path.toFile())){
            ZipEntry packMetaEntry = zipFile.getEntry("instrument_pack.meta.json");
            if (packMetaEntry == null) {
                YwzjMidi.LOGGER.error(MARKER,"Failed to load extension from ZIP {}. Error: {}", path.getFileName(), "No instrument_pack.meta.json found");
                return null;
            }

            try (InputStream stream = zipFile.getInputStream(packMetaEntry)) {
                PackMeta packMeta = GsonUtil.GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), PackMeta.class);

                if (packMeta == null) {
                    YwzjMidi.LOGGER.warn(MARKER, "Failed to read packMeta json: {}", path.getFileName());
                    return null;
                }

                if (packMeta.getNamespace() == null) {
                    YwzjMidi.LOGGER.warn(MARKER, "Failed to read namespace: {}", path.getFileName());
                    return null;
                }

                if (packMeta.getDependencies() != null && !modVersionAllMatch(packMeta)) {
                    YwzjMidi.LOGGER.warn(MARKER, "Mod version mismatch: {}", path.getFileName());
                    return null;
                }

                return new InstrumentPack(path, packMeta);
            } catch (IOException | JsonSyntaxException | JsonIOException | InvalidVersionSpecificationException e) {
                YwzjMidi.LOGGER.error(MARKER,"Failed to load extension from ZIP {}. Error: {}", path.getFileName(), e);
                return null;
            }
        } catch (IOException e) {
            YwzjMidi.LOGGER.error(MARKER,"Failed to load extension from ZIP {}. Error: {}", path.getFileName(), e);
            return null;
        }
    }

    private static List<InstrumentPack> scanInstrumentPacks(Path path) {
        List<InstrumentPack> instrumentPacks = new ArrayList<>();
        Set<String> namespaces = new HashSet<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)){
            for (Path entry : stream) {
                InstrumentPack instrumentPack = null;
                if (Files.isDirectory(entry)) {
                    instrumentPack = fromDirPath(entry);
                } else if (entry.toString().endsWith(".zip")) {
                    instrumentPack = fromZipPath(entry);
                }
                if (instrumentPack != null) {
                    if (namespaces.contains(instrumentPack.meta().getNamespace())) {
                        YwzjMidi.LOGGER.error(MARKER, "- {}, Duplicated namespace: {}", instrumentPack.path().getFileName(), instrumentPack.meta().getNamespace());
                        continue;
                    }
                    namespaces.add(instrumentPack.meta().getNamespace());
                    YwzjMidi.LOGGER.info(MARKER, "- {}, Main namespace: {}", instrumentPack.path().getFileName(), instrumentPack.meta().getNamespace());
                    instrumentPacks.add(instrumentPack);
                }
            }
        } catch (IOException e) {
            YwzjMidi.LOGGER.error(MARKER, "Failed to scan extensions from {}. Error: {}", path, e);
        }

        return instrumentPacks;
    }

    private static boolean modVersionAllMatch(PackMeta info) throws InvalidVersionSpecificationException {
        HashMap<String, String> dependencies = info.getDependencies();
        for (String modId : dependencies.keySet()) {
            if (!modVersionMatch(modId, dependencies.get(modId))) {
                return false;
            }
        }
        return true;
    }

    private static boolean modVersionMatch(String modId, String version) throws InvalidVersionSpecificationException {
        VersionRange versionRange = VersionRange.createFromVersionSpec(version);
        return ModList.get().getModContainerById(modId).map(mod -> {
            ArtifactVersion modVersion = mod.getModInfo().getVersion();
            return versionRange.containsVersion(modVersion);
        }).orElse(false);
    }

    public List<InstrumentPack> getInstrumentPacks() {
        return instrumentPacks;
    }

    public record InstrumentPack(Path path, PackMeta meta) {}

}

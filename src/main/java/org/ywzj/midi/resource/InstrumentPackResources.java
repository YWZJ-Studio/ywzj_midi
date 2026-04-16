package org.ywzj.midi.resource;

import cpw.mods.jarhandling.SecureJar;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.resource.PathPackResources;
import org.jetbrains.annotations.NotNull;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.util.SoundFileHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

public class InstrumentPackResources extends PathPackResources {

    private final InstrumentPackLoader.InstrumentPack instrumentPack;
    private final SecureJar secureJar;
    private final Map<String, byte[]> virtualSoundsJson;

    public InstrumentPackResources(InstrumentPackLoader.InstrumentPack instrumentPack) {
        super(instrumentPack.meta().getNamespace(), false, instrumentPack.path());
        this.instrumentPack = instrumentPack;
        this.secureJar = SecureJar.from(instrumentPack.path());
        this.virtualSoundsJson = SoundFileHelper.scanVirtualSoundsJson(instrumentPack.path(), instrumentPack.meta().getNamespace());
    }

    @Override
    protected @NotNull Path resolve(String... paths) {
        if (paths.length < 1) {
            throw new IllegalArgumentException("Missing path");
        }
        return secureJar.getPath(String.join("/", paths));
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type == PackType.CLIENT_RESOURCES && SoundFileHelper.isVirtualSoundsJson(location)) {
            byte[] bytes = virtualSoundsJson.get(location.getNamespace());
            if (bytes != null) {
                IoSupplier<InputStream> original = super.getResource(type, location);
                if (original != null) {
                    return () -> SoundFileHelper.mergeSoundsJson(original.get(), bytes);
                }
                return () -> new ByteArrayInputStream(bytes);
            }
        }
        return super.getResource(type, location);
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput resourceOutput) {
        super.listResources(type, namespace, path, resourceOutput);
        if (type != PackType.CLIENT_RESOURCES) {
            return;
        }
        if ((SoundFileHelper.SOUNDS_JSON.startsWith(path) || path.startsWith(SoundFileHelper.SOUNDS_JSON))
                && virtualSoundsJson.containsKey(namespace)) {
            byte[] bytes = virtualSoundsJson.get(namespace);
            ResourceLocation location = YwzjMidi.resourceLocation(namespace, SoundFileHelper.SOUNDS_JSON);
            resourceOutput.accept(location, () -> new ByteArrayInputStream(bytes));
        }
    }

    public InstrumentPackLoader.InstrumentPack getInstrumentPack() {
        return instrumentPack;
    }

}

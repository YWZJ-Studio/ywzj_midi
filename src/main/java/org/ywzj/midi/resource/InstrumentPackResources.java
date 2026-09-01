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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
        if (type == PackType.CLIENT_RESOURCES
                && instrumentPack.meta().getNamespace().equals(location.getNamespace())
                && "pack.png".equals(location.getPath())) {
            IoSupplier<InputStream> packIcon = getRootResource("pack.png");
            if (packIcon != null) {
                return packIcon;
            }
        }
        if (type == PackType.CLIENT_RESOURCES && SoundFileHelper.isVirtualSoundsJson(location)) {
            byte[] bytes = virtualSoundsJson.get(location.getNamespace());
            if (bytes != null) {
                IoSupplier<InputStream> original = super.getResource(type, location);
                if (original == null) {
                    return () -> new ByteArrayInputStream(bytes);
                }
                return () -> SoundFileHelper.mergeSoundsJson(original.get(), bytes);
            }
        }
        return super.getResource(type, location);
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        Set<String> namespaces = super.getNamespaces(type);
        if (type != PackType.CLIENT_RESOURCES || virtualSoundsJson.isEmpty()) {
            return namespaces;
        }
        Set<String> result = new LinkedHashSet<>(namespaces);
        result.addAll(virtualSoundsJson.keySet());
        return Set.copyOf(result);
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput resourceOutput) {
        super.listResources(type, namespace, path, resourceOutput);
        if (type != PackType.CLIENT_RESOURCES) {
            return;
        }
        if ((SoundFileHelper.SOUNDS_JSON.startsWith(path) || path.startsWith(SoundFileHelper.SOUNDS_JSON))
                && virtualSoundsJson.containsKey(namespace)) {
            ResourceLocation location = YwzjMidi.resourceLocation(namespace, SoundFileHelper.SOUNDS_JSON);
            IoSupplier<InputStream> resource = getResource(type, location);
            if (resource != null) {
                resourceOutput.accept(location, resource);
            }
        }
    }

    public InstrumentPackLoader.InstrumentPack getInstrumentPack() {
        return instrumentPack;
    }

}

package org.ywzj.midi.resource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.util.SoundFileHelper;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class InstrumentPackResources extends AbstractPackResources {

    private final InstrumentPackLoader.InstrumentPack instrumentPack;
    private final PackResources delegate;
    private final Map<String, byte[]> virtualSoundsJson;

    public InstrumentPackResources(PackLocationInfo info, InstrumentPackLoader.InstrumentPack instrumentPack) {
        super(info);
        this.instrumentPack = instrumentPack;
        this.delegate = Files.isDirectory(instrumentPack.path())
                ? new PathPackResources.PathResourcesSupplier(instrumentPack.path()).openPrimary(info)
                : new FilePackResources.FileResourcesSupplier(instrumentPack.path()).openPrimary(info);
        this.virtualSoundsJson = SoundFileHelper.scanVirtualSoundsJson(
                instrumentPack.path(), instrumentPack.meta().getNamespace());
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... paths) {
        return delegate.getRootResource(paths);
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type == PackType.CLIENT_RESOURCES && SoundFileHelper.isVirtualSoundsJson(location)) {
            byte[] bytes = virtualSoundsJson.get(location.getNamespace());
            if (bytes != null) {
                IoSupplier<InputStream> original = delegate.getResource(type, location);
                if (original == null) {
                    return () -> new ByteArrayInputStream(bytes);
                }
                return () -> SoundFileHelper.mergeSoundsJson(original.get(), bytes);
            }
        }
        return delegate.getResource(type, location);
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        delegate.listResources(type, namespace, path, output);
        if (type == PackType.CLIENT_RESOURCES
                && (SoundFileHelper.SOUNDS_JSON.startsWith(path) || path.startsWith(SoundFileHelper.SOUNDS_JSON))
                && virtualSoundsJson.containsKey(namespace)) {
            ResourceLocation location = YwzjMidi.resourceLocation(namespace, SoundFileHelper.SOUNDS_JSON);
            IoSupplier<InputStream> resource = getResource(type, location);
            if (resource != null) {
                output.accept(location, resource);
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        Set<String> namespaces = delegate.getNamespaces(type);
        if (type != PackType.CLIENT_RESOURCES || virtualSoundsJson.isEmpty()) {
            return namespaces;
        }
        Set<String> result = new LinkedHashSet<>(namespaces);
        result.addAll(virtualSoundsJson.keySet());
        return Set.copyOf(result);
    }

    @Override
    @Nullable
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) throws IOException {
        return delegate.getMetadataSection(serializer);
    }

    @Override
    public void close() {
        delegate.close();
    }

    public InstrumentPackLoader.InstrumentPack getInstrumentPack() {
        return instrumentPack;
    }

}

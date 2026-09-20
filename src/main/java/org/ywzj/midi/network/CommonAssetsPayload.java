package org.ywzj.midi.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** 通用乐器数据先整体压缩，再切片传输。 */
public record CommonAssetsPayload(Map<ResourceLocation, String> instrumentModels,
                                  Map<ResourceLocation, String> instrumentData) {

    /** 返回的缓冲区归调用方所有，切片后必须释放。 */
    public FriendlyByteBuf encode() {
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        try {
            data.writeMap(instrumentModels, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeUtf);
            data.writeMap(instrumentData, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeUtf);
            try (ByteArrayOutputStream output = new ByteArrayOutputStream();
                 GZIPOutputStream gzip = new GZIPOutputStream(output)) {
                data.getBytes(data.readerIndex(), gzip, data.readableBytes());
                gzip.finish();
                return new FriendlyByteBuf(Unpooled.wrappedBuffer(output.toByteArray()));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to gzip common instrument assets", exception);
        } finally {
            data.release();
        }
    }

    /** 不推进调用方压缩缓冲区的读索引，也不释放该缓冲区。 */
    public static CommonAssetsPayload decode(FriendlyByteBuf compressed) throws IOException {
        byte[] bytes = new byte[compressed.readableBytes()];
        compressed.getBytes(compressed.readerIndex(), bytes);
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.wrappedBuffer(gzip.readAllBytes()));
            try {
                var models = data.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readUtf);
                var instruments = data.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readUtf);
                return new CommonAssetsPayload(models, instruments);
            } finally {
                data.release();
            }
        }
    }

}

package org.ywzj.midi.custom.instrument;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 乐器配置数据类型，用于从数据包反序列化乐器参数。
 * 参照载具模组的 VehicleDataType 设计。
 *
 * @param <T> 数据类型
 */
public record InstrumentDataType<T extends BaseInstrumentData>(
        ResourceLocation id,
        DataSerializer<T> dataSerializer
) {
    @Nullable
    public T parse(@NotNull JsonElement json) {
        return dataSerializer.parse(json);
    }

    @NotNull
    public ResourceLocation getId() {
        return id;
    }

    @FunctionalInterface
    public interface DataSerializer<T extends BaseInstrumentData> {
        @Nullable
        T parse(JsonElement json);
    }

    public static class Builder<T extends BaseInstrumentData> {
        private final ResourceLocation id;
        private DataSerializer<T> dataSerializer;

        private Builder(ResourceLocation id) {
            this.id = id;
        }

        public static <T extends BaseInstrumentData> Builder<T> of(ResourceLocation id) {
            return new Builder<>(id);
        }

        public Builder<T> setDataSerializer(DataSerializer<T> dataSerializer) {
            this.dataSerializer = dataSerializer;
            return this;
        }

        public InstrumentDataType<T> build() {
            return new InstrumentDataType<>(id, dataSerializer);
        }
    }
}

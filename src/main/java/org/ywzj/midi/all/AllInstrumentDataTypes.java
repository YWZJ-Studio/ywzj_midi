package org.ywzj.midi.all;

import net.minecraft.resources.ResourceLocation;
import org.ywzj.midi.YwzjMidi;
import org.ywzj.midi.custom.instrument.*;
import org.ywzj.midi.custom.serialize.GsonUtil;

import java.util.LinkedHashMap;
import java.util.Map;

public class AllInstrumentDataTypes {

    private static final Map<ResourceLocation, InstrumentDataType<?>> TYPE_MAP = new LinkedHashMap<>();

    public static final InstrumentDataType<KeyboardInstrumentData> KEYBOARD = register(
            "keyboard",
            json -> {
                var pojo = GsonUtil.GSON.fromJson(json, BaseInstrumentDataPojo.class);
                var data = new KeyboardInstrumentData();
                data.build(pojo);
                return data;
            }
    );

    public static final InstrumentDataType<BrassInstrumentData> BRASS = register(
            "brass",
            json -> {
                var pojo = GsonUtil.GSON.fromJson(json, BaseInstrumentDataPojo.class);
                var data = new BrassInstrumentData();
                data.build(pojo);
                return data;
            }
    );

    public static final InstrumentDataType<WoodwindInstrumentData> WOODWIND = register(
            "woodwind",
            json -> {
                var pojo = GsonUtil.GSON.fromJson(json, BaseInstrumentDataPojo.class);
                var data = new WoodwindInstrumentData();
                data.build(pojo);
                return data;
            }
    );

    public static final InstrumentDataType<StringInstrumentData> STRING = register(
            "string",
            json -> {
                var pojo = GsonUtil.GSON.fromJson(json, BaseInstrumentDataPojo.class);
                var data = new StringInstrumentData();
                data.build(pojo);
                return data;
            }
    );

    public static final InstrumentDataType<PercussionInstrumentData> PERCUSSION = register(
            "percussion",
            json -> {
                var pojo = GsonUtil.GSON.fromJson(json, BaseInstrumentDataPojo.class);
                var data = new PercussionInstrumentData();
                data.build(pojo);
                return data;
            }
    );

    public static final InstrumentDataType<PluckedInstrumentData> PLUCKED = register(
            "plucked",
            json -> {
                var pojo = GsonUtil.GSON.fromJson(json, BaseInstrumentDataPojo.class);
                var data = new PluckedInstrumentData();
                data.build(pojo);
                return data;
            }
    );

    private static <T extends BaseInstrumentData> InstrumentDataType<T> register(String name, InstrumentDataType.DataSerializer<T> dataSerializer) {
        var dataType = InstrumentDataType.Builder.<T>of(YwzjMidi.modLocation(name))
                .setDataSerializer(dataSerializer)
                .build();
        TYPE_MAP.put(dataType.getId(), dataType);
        return dataType;
    }

    public static InstrumentDataType<?> getType(ResourceLocation id) {
        return TYPE_MAP.get(id);
    }

    public static void init() {}

}

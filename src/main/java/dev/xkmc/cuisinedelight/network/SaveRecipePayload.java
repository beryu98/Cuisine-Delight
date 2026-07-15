package dev.xkmc.cuisinedelight.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import dev.xkmc.cuisinedelight.init.CuisineDelight;

public record SaveRecipePayload() implements CustomPacketPayload {
    public static final Type<SaveRecipePayload> TYPE = new Type<>(CuisineDelight.loc("save_recipe"));
    public static final StreamCodec<ByteBuf, SaveRecipePayload> CODEC = StreamCodec.unit(new SaveRecipePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
package dev.xkmc.cuisinedelight.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import dev.xkmc.cuisinedelight.init.CuisineDelight;

import java.util.List;

public record CuisineBookPayload(List<RecipeEntry> recipes) implements CustomPacketPayload {

    public static final Type<CuisineBookPayload> TYPE = new Type<>(CuisineDelight.loc("cuisine_book_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeEntry> ENTRY_CODEC = StreamCodec.composite(
        ItemStack.STREAM_CODEC, RecipeEntry::result,
        ItemStack.STREAM_CODEC, RecipeEntry::in1,
        ItemStack.STREAM_CODEC, RecipeEntry::in2,
        ItemStack.STREAM_CODEC, RecipeEntry::in3,
        RecipeEntry::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CuisineBookPayload> CODEC = StreamCodec.composite(
        ENTRY_CODEC.apply(ByteBufCodecs.list()), CuisineBookPayload::recipes,
        CuisineBookPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record RecipeEntry(ItemStack result, ItemStack in1, ItemStack in2, ItemStack in3) {}
}
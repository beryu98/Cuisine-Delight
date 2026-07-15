package dev.xkmc.cuisinedelight.content.item;

import dev.xkmc.cuisinedelight.content.recipe.SimpleCuisineRecipeStorage;
import dev.xkmc.cuisinedelight.network.CuisineBookPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class CuisineRecipeBookItem extends Item {

    public CuisineRecipeBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            SimpleCuisineRecipeStorage storage = SimpleCuisineRecipeStorage.get(serverLevel);

            List<CuisineBookPayload.RecipeEntry> entries = new ArrayList<>();
            for (var recipe : storage.recipes()) {
                entries.add(new CuisineBookPayload.RecipeEntry(
                    recipe.result().getDefaultInstance(),
                    recipe.ingredients().size() > 0 ? recipe.ingredients().get(0).getDefaultInstance() : ItemStack.EMPTY,
                    recipe.ingredients().size() > 1 ? recipe.ingredients().get(1).getDefaultInstance() : ItemStack.EMPTY,
                    recipe.ingredients().size() > 2 ? recipe.ingredients().get(2).getDefaultInstance() : ItemStack.EMPTY
                ));
            }

            PacketDistributor.sendToPlayer(serverPlayer, new CuisineBookPayload(entries));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
package dev.xkmc.cuisinedelight.content.item;

import dev.xkmc.cuisinedelight.content.block.CuisineSkilletBlockEntity;
import dev.xkmc.cuisinedelight.content.logic.CookingData;
import dev.xkmc.cuisinedelight.content.recipe.SimpleCuisineRecipeStorage;
import dev.xkmc.cuisinedelight.init.registrate.CDItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class PlateItem extends Item {

	public interface ReturnTarget {

		void addItem(ItemStack foodStack);

	}

	public record PlayerTarget(Player player) implements ReturnTarget {

		@Override
		public void addItem(ItemStack foodStack) {
			player.getInventory().placeItemBackInInventory(foodStack);
		}

	}

	public record BlockTarget(UseOnContext ctx) implements ReturnTarget {

		@Override
		public void addItem(ItemStack foodStack) {
			Block.popResource(ctx.getLevel(), ctx.getClickedPos(), foodStack);
		}
	}

	public PlateItem(Properties pProperties) {
		super(pProperties);
	}

	private void giveBack(ItemStack foodStack, ReturnTarget target) {
		target.addItem(foodStack);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack plateStack = player.getItemInHand(hand);
		InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
		ItemStack skilletStack = player.getItemInHand(otherHand);
		if (!skilletStack.is(CDItems.SKILLET.get())) {
			return InteractionResultHolder.pass(plateStack);
		}
		CookingData data = CuisineSkilletItem.getData(skilletStack);
		if (data == null || !data.isComplete()) {
			return InteractionResultHolder.pass(plateStack);
		}
		if (!level.isClientSide()) {
			CuisineSkilletItem.setData(skilletStack, null);
			ItemStack foodStack = SimpleCuisineRecipeStorage.get((ServerLevel) level).find(data.contents);
			plateStack.shrink(1);
			giveBack(foodStack, new PlayerTarget(player));
		}
		return InteractionResultHolder.success(plateStack);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		Player player = ctx.getPlayer();
		if (level.getBlockEntity(ctx.getClickedPos()) instanceof CuisineSkilletBlockEntity be) {
			if (be.cookingData.contents.isEmpty() || !be.cookingData.isComplete()) {
				return InteractionResult.PASS;
			}
			if (!level.isClientSide()) {
				CookingData data = be.cookingData;
				ItemStack foodStack = SimpleCuisineRecipeStorage.get((ServerLevel) level).find(data.contents);
				ctx.getItemInHand().shrink(1);
				if (player != null) {
					giveBack(foodStack, new PlayerTarget(player));
				} else {
					giveBack(foodStack, new BlockTarget(ctx));
				}
				be.cookingData = new CookingData();
				be.sync();
				be.setChanged();
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
}

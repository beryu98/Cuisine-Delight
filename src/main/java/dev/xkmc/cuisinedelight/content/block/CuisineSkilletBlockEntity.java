package dev.xkmc.cuisinedelight.content.block;

import dev.xkmc.cuisinedelight.content.item.CuisineSkilletItem;
import dev.xkmc.cuisinedelight.content.logic.CookingData;
import dev.xkmc.cuisinedelight.init.data.TagGen;
import dev.xkmc.cuisinedelight.init.data.TagRef;
import dev.xkmc.cuisinedelight.init.registrate.CDItems;
import dev.xkmc.l2core.base.tile.BaseBlockEntity;
import dev.xkmc.l2serial.serialization.marker.SerialClass;
import dev.xkmc.l2serial.serialization.marker.SerialField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.entity.HeatableBlockEntity;

import dev.xkmc.cuisinedelight.content.recipe.SimpleCuisineRecipeStorage;
import net.minecraft.server.level.ServerLevel;


import javax.annotation.Nonnull;

@SerialClass
public class CuisineSkilletBlockEntity extends BaseBlockEntity implements HeatableBlockEntity {

	@SerialField
	public ItemStack baseItem = CDItems.SKILLET.asStack();

	@Nonnull
	@SerialField
	public CookingData cookingData = new CookingData();

	@SerialField
	private int flipTimer = 0;

	public CuisineSkilletBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
		if (flipTimer > 0) {
			flipTimer--;
		}
		boolean wasFailed = cookingData.failed;
		boolean wasComplete = cookingData.isComplete();
		if (canCook()) {
			cookingData.tick();
		}

		if (!pLevel.isClientSide() && cookingData.isComplete()) {
			ItemStack foodStack = SimpleCuisineRecipeStorage.get((ServerLevel) pLevel).find(cookingData.contents);
			Block.popResource(pLevel, pPos, foodStack);
			cookingData = new CookingData();
			sync();
			setChanged();
			return;
		}

		if (wasFailed != cookingData.failed || wasComplete != cookingData.isComplete()) {
			sync();
			setChanged();
		}
	}

	public boolean isCooking() {
		return !cookingData.contents.isEmpty();
	}

	public NonNullList<ItemStack> getItems() {
		NonNullList<ItemStack> ans = NonNullList.create();
		if (!cookingData.failed) {
			ans.addAll(cookingData.contents);
		}
		return ans;
	}

	public void setSkilletItem(ItemStack stack) {
		baseItem = stack.copy();
		var data = CuisineSkilletItem.getData(stack);
		if (data != null) {
			cookingData = data;
		}
		CuisineSkilletItem.setData(baseItem, null);
		sync();
	}

	public ItemStack toItemStack() {
		ItemStack ans = baseItem.copy();
		if (!cookingData.contents.isEmpty()) {
			CuisineSkilletItem.setData(ans, cookingData);
		}
		return ans;
	}

	public boolean canCook() {
		return this.level != null && this.isHeated(this.level, this.getBlockPos());
	}

	public boolean slowCook() {
		if (level == null) return false;
		if (isHeated(this.level, this.getBlockPos())) {
			BlockState below = level.getBlockState(getBlockPos().below());
			return !below.is(TagRef.HEAT_SOURCES) || below.is(TagGen.LOW_HEAT);
		}
		return false;
	}

	public float getStirPercent(float pTick) {
		return Math.max(0, flipTimer - pTick) / 20;
	}

	public boolean flip() {
		boolean success = cookingData.flip();
		flipTimer = 20;
		sync();
		setChanged();
		return success;
	}
}
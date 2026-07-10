package dev.xkmc.cuisinedelight.content.client;

import dev.xkmc.cuisinedelight.content.block.CuisineSkilletBlockEntity;
import dev.xkmc.cuisinedelight.content.item.CuisineSkilletItem;
import dev.xkmc.cuisinedelight.content.logic.CookingData;
import dev.xkmc.cuisinedelight.init.data.CDConfig;
import dev.xkmc.cuisinedelight.init.registrate.CDItems;
import dev.xkmc.l2core.util.Proxy;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class CookingOverlay implements LayeredDraw.Layer {

	@Nullable
	private static CookingData getHandData() {
		LocalPlayer player = Proxy.getClientPlayer();
		ItemStack mainStack = player.getMainHandItem();
		ItemStack offStack = player.getOffhandItem();
		ItemStack stack;
		if (!mainStack.isEmpty() && mainStack.is(CDItems.SKILLET.get())) stack = mainStack;
		else if (!offStack.isEmpty() && offStack.is(CDItems.SKILLET.get())) stack = offStack;
		else return null;
		return CuisineSkilletItem.getData(stack);
	}

	@Nullable
	private static CookingData getBlockData() {
		HitResult result = Minecraft.getInstance().hitResult;
		if (result == null || result.getType() != HitResult.Type.BLOCK) return null;
		BlockPos pos = ((BlockHitResult) result).getBlockPos();
		if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof CuisineSkilletBlockEntity be) {
			return be.cookingData;
		}
		return null;
	}

	@Nullable
	public static CookingData getData() {
		CookingData itemData = getHandData();
		if (itemData != null) return itemData;
		return getBlockData();
	}

	@Override
	public void render(GuiGraphics g, DeltaTracker delta) {
		if (Minecraft.getInstance().level == null) return;
		CookingData data = getData();
		if (data == null || data.contents.isEmpty()) return;
		float scale = (float) (double) CDConfig.CLIENT.uiScale.get() * g.guiHeight() / 240;
		g.pose().pushPose();
		g.pose().scale(scale, scale, scale);
		int w = (int) (g.guiWidth() / scale);
		int h = (int) (g.guiHeight() / scale);

		int xa = CDConfig.CLIENT.uiXAnchor.get();
		int xo = CDConfig.CLIENT.uiXOffset.get();
		int ya = CDConfig.CLIENT.uiYAnchor.get();
		int yo = CDConfig.CLIENT.uiYOffset.get();
		int tw = 84;
		int th = 38;

		final int x = xo + (xa + 1) * (w - tw) / 2;
		final int y = yo + (ya + 1) * (h - th) / 2;

		Font font = Minecraft.getInstance().font;
		for (int i = 0; i < data.contents.size(); i++) {
			ItemStack stack = data.contents.get(i);
			g.renderItem(stack, x + i * 18, y);
			g.renderItemDecorations(font, stack, x + i * 18, y);
		}

		PieRenderer gauge = new PieRenderer(g, x + 66, y + 9);

		float warn = CookingData.WARNING_START / (float) CookingData.RED_TIME;
		float red = CookingData.RED_START / (float) CookingData.RED_TIME;
		gauge.fillPie(0, warn, PieRenderer.Texture.PIE_GREEN);
		gauge.fillPie(warn, red, PieRenderer.Texture.PIE_YELLOW);
		gauge.fillPie(red, 1, PieRenderer.Texture.PIE_RED);
		if (data.failed) {
			gauge.fillPie(0, 1, PieRenderer.Texture.PIE_RED);
		}
		gauge.drawNeedle(PieRenderer.Texture.NEEDLE_BLACK, data.dangerProgress());
		gauge.drawIcon(data.isComplete() ? PieRenderer.Texture.COOK : PieRenderer.Texture.FLIP);
		g.pose().popPose();
	}
}

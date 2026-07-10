package dev.xkmc.cuisinedelight.content.logic;

import dev.xkmc.l2serial.serialization.marker.SerialClass;
import dev.xkmc.l2serial.serialization.marker.SerialField;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

@SerialClass
public class CookingData {

	public static final int MAX_INGREDIENTS = 3;
	public static final int COMPLETE_TIME = 30 * 20;

	public static final int WARNING_START = 86;
	public static final int RED_START = 110;
	public static final int RED_TIME = 126;

	public static final int FLIP_REDUCTION = 5 * 20;

	@SerialField
	public ArrayList<ItemStack> contents = new ArrayList<>();

	@SerialField
	public boolean started = false;

	@SerialField
	public int cookTicks = 0;

	@SerialField
	public int dangerTicks = 0;

	@SerialField
	public boolean failed = false;

	public void addItem(ItemStack item) {
		if (failed) {
			started = false;
			cookTicks = 0;
			dangerTicks = 0;
			failed = false;
			contents.clear();
		}
		if (started || contents.size() >= MAX_INGREDIENTS) return;
		ItemStack copy = item.copy();
		copy.setCount(1);
		contents.add(copy);
		if (contents.size() >= MAX_INGREDIENTS) {
			started = true;
		}
	}

	public void tick() {
		if (!started || failed || isComplete()) return;
		cookTicks++;
		dangerTicks++;

		if (dangerTicks >= RED_START) {
			failed = true;
			contents.clear();
		}
	}

	public boolean canFlip() {
		return started && !failed && !isComplete() && dangerTicks >= WARNING_START && dangerTicks < RED_START;
	}

	public boolean flip() {
		if (!canFlip()) return false;
		dangerTicks = Math.max(0, dangerTicks - FLIP_REDUCTION);
		cookTicks = Math.min(COMPLETE_TIME, cookTicks + FLIP_REDUCTION);
		return true;
	}

	public boolean isComplete() {
		return started && cookTicks >= COMPLETE_TIME && !failed;
	}

	public float cookProgress() {
		return Math.min(1, cookTicks / (float) COMPLETE_TIME);
	}

	public float dangerProgress() {
		return Math.min(1, dangerTicks / (float) RED_TIME);
	}

	public Record immutable() {
		ArrayList<ItemStack> list = new ArrayList<>();
		for (var e : contents) list.add(e.copy());
		return new Record(list, started, cookTicks, dangerTicks, failed);
	}

	public record Record(ArrayList<ItemStack> contents, boolean started, int cookTicks, int dangerTicks, boolean failed) {

		public CookingData mutable() {
			var ans = new CookingData();
			for (var e : contents) ans.contents.add(e.copy());
			ans.started = started;
			ans.cookTicks = cookTicks;
			ans.dangerTicks = dangerTicks;
			ans.failed = failed;
			return ans;
		}

	}

}
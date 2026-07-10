package dev.xkmc.cuisinedelight.content.recipe;

import dev.xkmc.cuisinedelight.init.CuisineDelight;
import dev.xkmc.cuisinedelight.init.registrate.PlateFood;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SimpleCuisineRecipeStorage extends SavedData {

	private static final String DATA_NAME = CuisineDelight.MODID + "_recipes";

	private final List<Entry> recipes = new ArrayList<>();

	public static SimpleCuisineRecipeStorage get(ServerLevel level) {
		return level.getServer().overworld().getDataStorage().computeIfAbsent(
				new SavedData.Factory<>(SimpleCuisineRecipeStorage::new, SimpleCuisineRecipeStorage::load),
				DATA_NAME
		);
	}

	private static SimpleCuisineRecipeStorage load(CompoundTag tag, HolderLookup.Provider provider) {
		var ans = new SimpleCuisineRecipeStorage();
		ListTag list = tag.getList("recipes", Tag.TAG_COMPOUND);
		for (Tag raw : list) {
			if (!(raw instanceof CompoundTag recipeTag)) continue;
			Optional<Item> result = readItem(recipeTag.getString("result"));
			Optional<Item> first = readItem(recipeTag.getString("first"));
			Optional<Item> second = readItem(recipeTag.getString("second"));
			Optional<Item> third = readItem(recipeTag.getString("third"));
			if (result.isPresent() && first.isPresent() && second.isPresent() && third.isPresent()) {
				ans.recipes.add(new Entry(result.get(), sorted(first.get(), second.get(), third.get())));
			}
		}
		return ans;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (Entry recipe : recipes) {
			CompoundTag recipeTag = new CompoundTag();
			recipeTag.putString("result", id(recipe.result()));
			recipeTag.putString("first", id(recipe.ingredients().get(0)));
			recipeTag.putString("second", id(recipe.ingredients().get(1)));
			recipeTag.putString("third", id(recipe.ingredients().get(2)));
			list.add(recipeTag);
		}
		tag.put("recipes", list);
		return tag;
	}

	public void add(Item result, Item first, Item second, Item third) {
		List<Item> key = sorted(first, second, third);
		recipes.removeIf(e -> e.ingredients().equals(key));
		recipes.add(new Entry(result, key));
		setDirty();
	}

	public int clear() {
		int count = recipes.size();
		recipes.clear();
		setDirty();
		return count;
	}

	public int size() {
		return recipes.size();
	}

	public List<Entry> recipes() {
		return List.copyOf(recipes);
	}

	public ItemStack find(List<ItemStack> stacks) {
		if (stacks.size() != 3) {
			return PlateFood.SUSPICIOUS_MIX.item.get().getDefaultInstance();
		}
		List<Item> key = sorted(stacks.get(0).getItem(), stacks.get(1).getItem(), stacks.get(2).getItem());
		for (Entry recipe : recipes) {
			if (recipe.ingredients().equals(key)) {
				return recipe.result().getDefaultInstance();
			}
		}
		return PlateFood.SUSPICIOUS_MIX.item.get().getDefaultInstance();
	}

	private static List<Item> sorted(Item first, Item second, Item third) {
		ArrayList<Item> ans = new ArrayList<>(List.of(first, second, third));
		ans.sort(Comparator.comparing(SimpleCuisineRecipeStorage::id));
		return List.copyOf(ans);
	}

	private static String id(Item item) {
		return BuiltInRegistries.ITEM.getKey(item).toString();
	}

	private static Optional<Item> readItem(String id) {
		ResourceLocation loc = ResourceLocation.tryParse(id);
		if (loc == null) return Optional.empty();
		return BuiltInRegistries.ITEM.getOptional(loc);
	}

	public record Entry(Item result, List<Item> ingredients) {
	}

}

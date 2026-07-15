package dev.xkmc.cuisinedelight.content.recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class SimpleCuisineRecipeStorage {

	private static final File FILE = FMLPaths.CONFIGDIR.get().resolve("cuisinedelight_recipes.json").toFile();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static final SimpleCuisineRecipeStorage INSTANCE = new SimpleCuisineRecipeStorage();

	public record Recipe(Item result, List<Item> ingredients) {}
	private final List<Recipe> recipes = new ArrayList<>();

	public static SimpleCuisineRecipeStorage get(Level level) {
		return INSTANCE;
	}

	public SimpleCuisineRecipeStorage() {
		load();
	}

	public void load() {
		recipes.clear();
		if (!FILE.exists()) return;
		try (FileReader reader = new FileReader(FILE)) {
			List<RecipeData> data = GSON.fromJson(reader, new TypeToken<List<RecipeData>>(){}.getType());
			if (data != null) {
				for (RecipeData rd : data) {
					Item res = BuiltInRegistries.ITEM.get(ResourceLocation.parse(rd.result));
					List<Item> ings = rd.ingredients.stream().map(id -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))).toList();
					recipes.add(new Recipe(res, ings));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void save() {
		try (FileWriter writer = new FileWriter(FILE)) {
			List<RecipeData> data = recipes.stream().map(r -> new RecipeData(
				BuiltInRegistries.ITEM.getKey(r.result()).toString(),
				r.ingredients().stream().map(i -> BuiltInRegistries.ITEM.getKey(i).toString()).toList()
			)).toList();
			GSON.toJson(data, writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void add(Item result, Item first, Item second, Item third) {
		List<Item> ings = new ArrayList<>();
		if (first != null) ings.add(first);
		if (second != null) ings.add(second);
		if (third != null) ings.add(third);
		recipes.add(new Recipe(result, ings));
		save();
	}

	public int remove(Item result) {
		int before = recipes.size();
		recipes.removeIf(r -> r.result() == result);
		save();
		return before - recipes.size();
	}

	public int clear() {
		int count = recipes.size();
		recipes.clear();
		save();
		return count;
	}

	public ItemStack find(List<ItemStack> inputs) {
		for (Recipe recipe : recipes) {
			if (recipe.ingredients().size() != inputs.size()) continue;
			boolean match = true;
			for (int i = 0; i < inputs.size(); i++) {
				if (recipe.ingredients().get(i) != inputs.get(i).getItem()) {
					match = false;
					break;
				}
			}
			if (match) return recipe.result().getDefaultInstance();
		}
		return ItemStack.EMPTY;
	}

	public List<Recipe> recipes() {
		return recipes;
	}

	private record RecipeData(String result, List<String> ingredients) {}
}
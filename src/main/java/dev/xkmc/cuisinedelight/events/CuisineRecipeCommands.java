package dev.xkmc.cuisinedelight.events;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.xkmc.cuisinedelight.content.logic.IngredientConfig;
import dev.xkmc.cuisinedelight.content.recipe.SimpleCuisineRecipeStorage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.LinkedHashSet;
import java.util.concurrent.CompletableFuture;

public class CuisineRecipeCommands {

	private static final DynamicCommandExceptionType INVALID_ITEM = new DynamicCommandExceptionType(
		id -> Component.literal("Unknown item: " + id)
	);

	public static void register(RegisterCommandsEvent event) {
		register(event.getDispatcher());
	}

	private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("cuisine_recipe")
			.requires(source -> source.hasPermission(2))
			.then(Commands.literal("add")
				.then(Commands.argument("result", ResourceLocationArgument.id())
					.suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ITEM.keySet(), builder))
					.then(Commands.argument("first", ResourceLocationArgument.id())
						.suggests((ctx, builder) -> suggestIngredients(builder))
						.then(Commands.argument("second", ResourceLocationArgument.id())
							.suggests((ctx, builder) -> suggestIngredients(builder))
							.then(Commands.argument("third", ResourceLocationArgument.id())
								.suggests((ctx, builder) -> suggestIngredients(builder))
								.executes(CuisineRecipeCommands::add))))))
			.then(Commands.literal("list").executes(CuisineRecipeCommands::list))
			.then(Commands.literal("clear").executes(CuisineRecipeCommands::clear)));
	}

	private static int add(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Item result = getItem(ctx, "result");
		Item first = getItem(ctx, "first");
		Item second = getItem(ctx, "second");
		Item third = getItem(ctx, "third");

		SimpleCuisineRecipeStorage.get(ctx.getSource().getLevel())
			.add(result, first, second, third);
		ctx.getSource().sendSuccess(() -> Component.literal("Cuisine recipe saved."), true);
		return 1;
	}

	private static int list(CommandContext<CommandSourceStack> ctx) {
		SimpleCuisineRecipeStorage data = SimpleCuisineRecipeStorage.get(ctx.getSource().getLevel());
		ctx.getSource().sendSuccess(() -> Component.literal("Cuisine recipes: " + data.size()), false);
		for (var recipe : data.recipes()) {
			String text = id(recipe.ingredients().get(0)) + " + " +
				id(recipe.ingredients().get(1)) + " + " +
				id(recipe.ingredients().get(2)) + " = " +
				id(recipe.result());
			ctx.getSource().sendSuccess(() -> Component.literal(text), false);
		}
		return data.size();
	}

	private static int clear(CommandContext<CommandSourceStack> ctx) {
		int count = SimpleCuisineRecipeStorage.get(ctx.getSource().getLevel()).clear();
		ctx.getSource().sendSuccess(() -> Component.literal("Cleared " + count + " cuisine recipes."), true);
		return count;
	}

	private static Item getItem(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
		ResourceLocation loc = ResourceLocationArgument.getId(ctx, name);
		return BuiltInRegistries.ITEM.getOptional(loc)
			.orElseThrow(() -> INVALID_ITEM.create(loc));
	}

	private static String id(Item item) {
		return BuiltInRegistries.ITEM.getKey(item).toString();
	}

	private static CompletableFuture<Suggestions> suggestIngredients(SuggestionsBuilder builder) {
		LinkedHashSet<ResourceLocation> ideas = new LinkedHashSet<>();
		for (var entry : IngredientConfig.get().entries) {
			for (var stack : entry.ingredient.getItems()) {
				ideas.add(BuiltInRegistries.ITEM.getKey(stack.getItem()));
			}
		}
		return SharedSuggestionProvider.suggestResource(ideas, builder);
	}

}
package dev.xkmc.cuisinedelight.events;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import dev.xkmc.cuisinedelight.content.recipe.SimpleCuisineRecipeStorage;
import dev.xkmc.cuisinedelight.content.menu.RecipeAddMenu;

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
			.then(Commands.literal("add").executes(CuisineRecipeCommands::openAddGui))
			.then(Commands.literal("remove")
				.then(Commands.argument("result", ResourceLocationArgument.id())
					.suggests((ctx, builder) -> SharedSuggestionProvider.suggestResource(
						SimpleCuisineRecipeStorage.INSTANCE.recipes().stream().map(r -> BuiltInRegistries.ITEM.getKey(r.result())), builder))
					.executes(CuisineRecipeCommands::remove)))
			.then(Commands.literal("list").executes(CuisineRecipeCommands::list))
			.then(Commands.literal("clear").executes(CuisineRecipeCommands::clear)));
	}

	private static int openAddGui(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		player.openMenu(new SimpleMenuProvider((id, inv, p) -> new RecipeAddMenu(id, inv), Component.literal("레시피 등록")));
		return 1;
	}

	private static int remove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Item result = getItem(ctx, "result");
		int removed = SimpleCuisineRecipeStorage.INSTANCE.remove(result);
		ctx.getSource().sendSuccess(() -> Component.literal(removed + "개의 레시피가 삭제되었습니다."), true);
		return removed;
	}

	private static int list(CommandContext<CommandSourceStack> ctx) {
		SimpleCuisineRecipeStorage data = SimpleCuisineRecipeStorage.INSTANCE;
		ctx.getSource().sendSuccess(() -> Component.literal("Cuisine recipes: " + data.recipes().size()), false);
		for (var recipe : data.recipes()) {
			String text = BuiltInRegistries.ITEM.getKey(recipe.result()) + " <- " + recipe.ingredients().size() + " ingredients";
			ctx.getSource().sendSuccess(() -> Component.literal(text), false);
		}
		return data.recipes().size();
	}

	private static int clear(CommandContext<CommandSourceStack> ctx) {
		int count = SimpleCuisineRecipeStorage.INSTANCE.clear();
		ctx.getSource().sendSuccess(() -> Component.literal("Cleared " + count + " cuisine recipes."), true);
		return count;
	}

	private static Item getItem(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
		ResourceLocation loc = ResourceLocationArgument.getId(ctx, name);
		return BuiltInRegistries.ITEM.getOptional(loc).orElseThrow(() -> INVALID_ITEM.create(loc));
	}
}
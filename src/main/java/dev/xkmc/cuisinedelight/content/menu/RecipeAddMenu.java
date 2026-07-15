package dev.xkmc.cuisinedelight.content.menu;

import dev.xkmc.cuisinedelight.content.recipe.SimpleCuisineRecipeStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import dev.xkmc.cuisinedelight.init.registrate.CDMisc; // 메뉴 타입이 등록된 곳

public class RecipeAddMenu extends AbstractContainerMenu {

    public final Container container = new SimpleContainer(4);
    private final Player player;

    public RecipeAddMenu(int containerId, Inventory playerInv) {
        super(CDMisc.RECIPE_ADD_MENU.get(), containerId);
        this.player = playerInv.player;

        this.addSlot(new Slot(container, 0, 80, 15));

        this.addSlot(new Slot(container, 1, 44, 45));
        this.addSlot(new Slot(container, 2, 80, 45));
        this.addSlot(new Slot(container, 3, 116, 45));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInv, k, 8 + k * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public void saveRecipe() {
        Item result = container.getItem(0).getItem();
        Item in1 = container.getItem(1).getItem();
        Item in2 = container.getItem(2).getItem();
        Item in3 = container.getItem(3).getItem();

        if (!container.getItem(0).isEmpty() && !container.getItem(1).isEmpty()) {
            SimpleCuisineRecipeStorage.INSTANCE.add(result, in1,
                container.getItem(2).isEmpty() ? null : in2,
                container.getItem(3).isEmpty() ? null : in3);
            player.sendSystemMessage(Component.literal("레시피가 저장되었습니다!"));
        }

        for (int i=0; i<4; i++) player.getInventory().placeItemBackInInventory(container.getItem(i));
        player.closeContainer();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player, this.container);
    }
}
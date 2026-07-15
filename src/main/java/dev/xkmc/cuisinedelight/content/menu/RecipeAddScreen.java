package dev.xkmc.cuisinedelight.content.menu;

import dev.xkmc.cuisinedelight.network.SaveRecipePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class RecipeAddScreen extends AbstractContainerScreen<RecipeAddMenu> {

    private static final ResourceLocation BG = ResourceLocation.withDefaultNamespace("textures/gui/container/dispenser.png");

    public RecipeAddScreen(RecipeAddMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("확인"), btn -> {
            PacketDistributor.sendToServer(new SaveRecipePayload());
        }).bounds(this.leftPos + 68, this.topPos + 66, 40, 16).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(BG, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        g.fill(leftPos + 60, topPos + 15, leftPos + 116, topPos + 71, 0xFFC6C6C6);

        g.blit(BG, leftPos + 79, topPos + 14, 7, 83, 18, 18);
        g.blit(BG, leftPos + 43, topPos + 44, 7, 83, 18, 18);
        g.blit(BG, leftPos + 79, topPos + 44, 7, 83, 18, 18);
        g.blit(BG, leftPos + 115, topPos + 44, 7, 83, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        String res = "완성";
        g.drawString(this.font, res, 88 - (this.font.width(res) / 2), 5, 0x404040, false);

        String in1 = "재료1";
        g.drawString(this.font, in1, 52 - (this.font.width(in1) / 2), 35, 0x404040, false);

        String in2 = "재료2";
        g.drawString(this.font, in2, 88 - (this.font.width(in2) / 2), 35, 0x404040, false);

        String in3 = "재료3";
        g.drawString(this.font, in3, 124 - (this.font.width(in3) / 2), 35, 0x404040, false);
    }
}
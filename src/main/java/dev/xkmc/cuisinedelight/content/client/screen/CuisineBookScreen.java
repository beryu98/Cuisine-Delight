package dev.xkmc.cuisinedelight.content.client.screen;

import dev.xkmc.cuisinedelight.network.CuisineBookPayload.RecipeEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CuisineBookScreen extends Screen {

    private static final ResourceLocation BOOK_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/book.png");
    private final List<RecipeEntry> recipes;

    private RecipeEntry selectedRecipe = null;

    private int leftPos, topPos;
    private final int imageWidth = 192;
    private final int imageHeight = 192;

    private PageButton backButton;

    public CuisineBookScreen(List<RecipeEntry> recipes) {
        super(Component.literal("요리 가이드북"));
        this.recipes = recipes;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        this.clearWidgets();

        int buttonX = this.leftPos + (this.imageWidth / 2) - 13;
        int buttonY = this.topPos + 155;

        this.backButton = this.addRenderableWidget(new PageButton(buttonX, buttonY, false, btn -> {
            this.selectedRecipe = null;
            this.updateButtons();
        }, true));

        this.updateButtons();
    }

    private void updateButtons() {
        if (this.backButton != null) {
            this.backButton.visible = (this.selectedRecipe != null);
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);

        g.blit(BOOK_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if (selectedRecipe == null) {
            renderIndexPage(g, mouseX, mouseY);
        } else {
            renderDetailPage(g, mouseX, mouseY);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderIndexPage(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, "요리 레시피 목록", leftPos + 45, topPos + 20, 0x000000, false);

        int startX = leftPos + 40;
        int startY = topPos + 40;
        int cols = 5;
        int spacingX = 22;
        int spacingY = 22;

        for (int i = 0; i < recipes.size(); i++) {
            RecipeEntry entry = recipes.get(i);
            int x = startX + (i % cols) * spacingX;
            int y = startY + (i / cols) * spacingY;

            g.renderItem(entry.result(), x, y);

            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                g.renderTooltip(this.font, entry.result(), mouseX, mouseY);
            }
        }
    }

    private void renderDetailPage(GuiGraphics g, int mouseX, int mouseY) {
        int visualCenterX = leftPos + imageWidth / 2 - 2;

        float resScale = 2.0f;
        int resSize = (int)(16 * resScale);
        int resX = visualCenterX - (resSize / 2);
        int resY = topPos + 40;

        g.pose().pushPose();
        g.pose().translate(resX, resY, 0);
        g.pose().scale(resScale, resScale, 1.0f);
        g.renderItem(selectedRecipe.result(), 0, 0);
        g.pose().popPose();

        if (mouseX >= resX && mouseX < resX + resSize && mouseY >= resY && mouseY < resY + resSize) {
            g.renderTooltip(this.font, selectedRecipe.result(), mouseX, mouseY);
        }

        g.drawCenteredString(this.font, "재료", visualCenterX, topPos + 90, 0x000000);

        float inScale = 1.5f;
        int inSize = (int)(16 * inScale);
        int spacing = inSize + 6;

        int startInX = visualCenterX - (spacing + inSize / 2);
        int inY = topPos + 110;

        ItemStack[] ingredients = new ItemStack[] {
            selectedRecipe.in1(), selectedRecipe.in2(), selectedRecipe.in3()
        };

        for (int i = 0; i < 3; i++) {
            net.minecraft.world.item.ItemStack stack = ingredients[i];
            if (stack.isEmpty()) continue;

            int x = startInX + (i * spacing);

            g.pose().pushPose();
            g.pose().translate(x, inY, 0);
            g.pose().scale(inScale, inScale, 1.0f);
            g.renderItem(stack, 0, 0);
            g.pose().popPose();

            if (mouseX >= x && mouseX < x + inSize && mouseY >= inY && mouseY < inY + inSize) {
                g.renderTooltip(this.font, stack, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (selectedRecipe == null && button == 0) {
            int startX = leftPos + 40;
            int startY = topPos + 40;
            int cols = 5;
            int spacingX = 22;
            int spacingY = 22;

            for (int i = 0; i < recipes.size(); i++) {
                int x = startX + (i % cols) * spacingX;
                int y = startY + (i / cols) * spacingY;

                if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                    this.selectedRecipe = recipes.get(i);
                    this.updateButtons();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isHovering(int x, int y, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
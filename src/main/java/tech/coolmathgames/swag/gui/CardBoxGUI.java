package tech.coolmathgames.swag.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import tech.coolmathgames.swag.OpenSwag;
import tech.coolmathgames.swag.driver.ContainerCardBox;
import tech.coolmathgames.swag.item.BlockCardBox;

public class CardBoxGUI extends GuiContainer {
  private final ContainerCardBox container;
  private static final ResourceLocation background = new ResourceLocation(OpenSwag.MODID, "textures/gui/container/" + BlockCardBox.NAME + ".png");
  public CardBoxGUI(ContainerCardBox container) {
    super(container);
    this.container = container;
  }


  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    this.drawDefaultBackground();
    super.drawScreen(mouseX, mouseY, partialTicks);
    this.renderHoveredToolTip(mouseX, mouseY);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float v, int i, int i1) {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

    this.mc.getTextureManager().bindTexture(background);

    int x = (this.width - this.xSize) / 2;
    int y = (this.height - this.ySize) / 2;

    this.drawTexturedModalRect(x, y, 0, 0, container.xSize, container.ySize);
    this.drawSlots();
  }

  private void drawSlots() {}
}

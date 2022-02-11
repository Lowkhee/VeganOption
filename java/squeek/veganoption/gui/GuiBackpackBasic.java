package squeek.veganoption.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import squeek.veganoption.backpack.ContainerBackpack;

public class GuiBackpackBasic extends GuiBackpack<ContainerBackpack> 
{

	public GuiBackpackBasic(ContainerBackpack container) 
	{
		this("textures/gui/backpack_basic.png", container);
	}

	protected GuiBackpackBasic(String texture, ContainerBackpack container) 
	{
		super(texture, container);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) 
	{
		bindTexture(textureFile);

		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.enableRescaleNormal();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft, guiTop, 0.0F);
		GlStateManager.popMatrix();

		bindTexture(textureFile);
	}
		
}

package squeek.veganoption.gui;

import java.io.IOException;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

public abstract class GuiBackpack <C extends Container> extends GuiContainer 
{
	protected final C container;

	public final ResourceLocation textureFile;

	protected GuiBackpack(String texture, C container) 
	{
		this(new ResourceLocation("veganoption:" + texture), container);
	}

	protected GuiBackpack(ResourceLocation texture, C container) 
	{
		super(container);

		this.textureFile = texture;

		this.container = container;

	}

	/* LEDGERS */
	@Override
	public void initGui() 
	{
		super.initGui();
	}

	@Override
	public void onGuiClosed() 
	{
		super.onGuiClosed();
	}

	public FontRenderer getFontRenderer() 
	{
		return fontRendererObj;
	}

	@Override
	protected void mouseClicked(int xPos, int yPos, int mouseButton) throws IOException
	{
		super.mouseClicked(xPos, yPos, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) 
	{
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long time) 
	{
		super.mouseClickMove(mouseX, mouseY, mouseButton, time);
	}

	@Nullable
	protected Slot getSlotAtPosition(int mouseX, int mouseY)
	{
		for (int k = 0; k < this.inventorySlots.inventorySlots.size(); ++k) 
		{
			Slot slot = this.inventorySlots.inventorySlots.get(k);
			if (isMouseOverSlot(slot, mouseX, mouseY))
				return slot;
		}

		return null;
	}

	private boolean isMouseOverSlot(Slot par1Slot, int mouseX, int mouseY) 
	{
		return isPointInRegion(par1Slot.xPos, par1Slot.yPos, 16, 16, mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) 
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


	protected void bindTexture(ResourceLocation texturePath) 
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
		textureManager.bindTexture(texturePath);
	}

	public void setZLevel(float level) {
		this.zLevel = level;
	}

	public int getSizeX() {
		return xSize;
	}

	public int getSizeY() {
		return ySize;
	}

	@Override
	public int getGuiLeft() {
		return guiLeft;
	}

	@Override
	public int getGuiTop() {
		return guiTop;
	}

	@Override
	public void drawGradientRect(int par1, int par2, int par3, int par4, int par5, int par6) {
		super.drawGradientRect(par1, par2, par3, par4, par5, par6);
	}
}

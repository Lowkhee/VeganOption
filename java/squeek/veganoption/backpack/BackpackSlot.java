package squeek.veganoption.backpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import squeek.veganoption.ModInfo;

public class BackpackSlot extends Slot 
{

	private boolean isPhantom;
	private boolean canAdjustPhantom = true;
	private boolean canShift = true;
	private int stackLimit;

	public BackpackSlot(IInventory inventory, int slotIndex, int xPos, int yPos) 
	{
		super(inventory, slotIndex, xPos, yPos);
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
			setBackgroundLocation(new ResourceLocation(ModInfo.MODID, "textures/gui/atlas"));
		
		this.stackLimit = -1;
	}

	public BackpackSlot setPhantom() 
	{
		isPhantom = true;
		return this;
	}

	public BackpackSlot blockShift() 
	{
		canShift = false;
		return this;
	}

	@Override
	public void putStack(ItemStack itemStack) 
	{
		if (!isPhantom() || canAdjustPhantom()) 
			super.putStack(itemStack);
	}

	public BackpackSlot setCanAdjustPhantom(boolean canAdjust) 
	{
		this.canAdjustPhantom = canAdjust;
		return this;
	}

	public BackpackSlot setStackLimit(int limit) 
	{
		this.stackLimit = limit;
		return this;
	}

	public boolean isPhantom() 
	{
		return this.isPhantom;
	}

	public boolean canAdjustPhantom() 
	{
		return canAdjustPhantom;
	}

	@Override
	public boolean canTakeStack(EntityPlayer stack) 
	{
		return !isPhantom();
	}

	public boolean canShift()
	{
		return canShift;
	}

	@Override
	public int getSlotStackLimit() 
	{
		if (stackLimit < 0)
			return super.getSlotStackLimit();
		else 
			return stackLimit;
	}

	public boolean isToolTipVisible() 
	{
		return getStack().isEmpty();
	}

	public boolean isMouseOver(int mouseX, int mouseY) 
	{
		return mouseX >= xPos && mouseX <= xPos + 16 && mouseY >= yPos && mouseY <= yPos + 16;
	}
}

package squeek.veganoption.content.registry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.Event;



public abstract class BottleRegistry 
{
	private static Map<List, FluidBottleData> bottleFluidMap = new HashMap();
    private static Map<List, FluidBottleData> filledBottleMap = new HashMap();

    
    private BottleRegistry() {}
    
    public static boolean registerFluidBottle(FluidStack fluidStack, ItemStack filledBottle)
    {
        return registerFluidBottle(new FluidBottleData(fluidStack, filledBottle));
    }
    
    public static boolean registerFluidBottle(FluidBottleData data)
    {
        
        bottleFluidMap.put(Arrays.asList(data.filledBottle.getItem(), data.filledBottle.getItemDamage()), data);

        filledBottleMap.put(Arrays.asList(FluidBottleData.EMPTY_BOTTLE.getItem(), FluidBottleData.EMPTY_BOTTLE.getItemDamage(), data.fluid.getFluid()), data);

        MinecraftForge.EVENT_BUS.post(new FluidBottleRegisterEvent(data));
        return true;
    }
    
    
    
    public static class FluidBottleData
    {
        public final FluidStack fluid;
        public final ItemStack filledBottle;
        public static final ItemStack EMPTY_BOTTLE = new ItemStack(Items.GLASS_BOTTLE);

        public FluidBottleData(FluidStack stack, ItemStack filledBottle)
        {
            this.fluid = stack;
            this.filledBottle = filledBottle;

            if (stack == null || filledBottle == null)
            {
                throw new RuntimeException("Invalid fluidBottle - a parameter was null.");
            }
        }

        public FluidBottleData copy()
        {
            return new FluidBottleData(fluid, filledBottle);
        }
    }

    public static class FluidBottleRegisterEvent extends Event
    {
        public final FluidBottleData data;

        public FluidBottleRegisterEvent(FluidBottleData data)
        {
            this.data = data.copy();
        }
    }
    
}



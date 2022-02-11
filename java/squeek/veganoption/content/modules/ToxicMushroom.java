package squeek.veganoption.content.modules;

import com.google.common.base.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import squeek.veganoption.ModInfo;
import squeek.veganoption.VeganOption;
import squeek.veganoption.content.ContentHelper;
import squeek.veganoption.content.IContentModule;
import squeek.veganoption.content.Modifiers;
import squeek.veganoption.content.modifiers.DropsModifier.BlockSpecifier;
import squeek.veganoption.content.modifiers.DropsModifier.DropSpecifier;

public class ToxicMushroom implements IContentModule
{
	public static Item falseMorel;
	public static Item falseMorelFermented;

	@Override
	public void create()
	{
		falseMorel = new ItemFood(2, 0.8F, false)
			.setPotionEffect(new PotionEffect(MobEffects.POISON, 5, 0), 1.0F)
			.setUnlocalizedName(ModInfo.MODID + ".falseMorel")
			.setCreativeTab(VeganOption.creativeTab)
			.setRegistryName(ModInfo.MODID, "falseMorel");
		GameRegistry.register(falseMorel);

		falseMorelFermented = new Item()
			.setUnlocalizedName(ModInfo.MODID + ".falseMorelFermented")
			.setCreativeTab(VeganOption.creativeTab)
			.setRegistryName(ModInfo.MODID, "falseMorelFermented");
		GameRegistry.register(falseMorelFermented);
	}

	@Override
	public void oredict()
	{
		//OreDictionary.registerOre(ContentHelper.poisonousOreDict, new ItemStack(Items.SPIDER_EYE));
		OreDictionary.registerOre(ContentHelper.poisonousOreDict, new ItemStack(falseMorel));
		//OreDictionary.registerOre(ContentHelper.fermentedOreDict, new ItemStack(Items.FERMENTED_SPIDER_EYE));
		OreDictionary.registerOre(ContentHelper.fermentedOreDict, new ItemStack(falseMorelFermented));
	}

	@Override
	public void recipes()
	{
		Modifiers.recipes.convertInput(new ItemStack(Items.SPIDER_EYE), ContentHelper.poisonousOreDict);
		Modifiers.recipes.excludeOutput(new ItemStack(Items.FERMENTED_SPIDER_EYE));
		Modifiers.recipes.convertInput(new ItemStack(Items.FERMENTED_SPIDER_EYE), ContentHelper.fermentedOreDict);
		
		Predicate<ItemStack> predicateFalseMorel = new PotionHelper.ItemPredicateInstance(falseMorel);
		Predicate<ItemStack> predicateFalseMorelFermented = new PotionHelper.ItemPredicateInstance(falseMorelFermented);
		
		PotionHelper.registerPotionTypeConversion(PotionTypes.WATER, predicateFalseMorel, PotionTypes.MUNDANE);
		PotionHelper.registerPotionTypeConversion(PotionTypes.AWKWARD, predicateFalseMorel, PotionTypes.POISON);
		
		PotionHelper.registerPotionTypeConversion(PotionTypes.NIGHT_VISION, predicateFalseMorelFermented, PotionTypes.INVISIBILITY);
		PotionHelper.registerPotionTypeConversion(PotionTypes.LONG_NIGHT_VISION, predicateFalseMorelFermented, PotionTypes.LONG_INVISIBILITY);
		PotionHelper.registerPotionTypeConversion(PotionTypes.LEAPING, predicateFalseMorelFermented, PotionTypes.SLOWNESS);
		PotionHelper.registerPotionTypeConversion(PotionTypes.LONG_LEAPING, predicateFalseMorelFermented, PotionTypes.LONG_SLOWNESS);
		PotionHelper.registerPotionTypeConversion(PotionTypes.SWIFTNESS, predicateFalseMorelFermented, PotionTypes.SLOWNESS);
		PotionHelper.registerPotionTypeConversion(PotionTypes.LONG_SWIFTNESS, predicateFalseMorelFermented, PotionTypes.LONG_SLOWNESS);
		PotionHelper.registerPotionTypeConversion(PotionTypes.HEALING, predicateFalseMorelFermented, PotionTypes.HARMING);
		PotionHelper.registerPotionTypeConversion(PotionTypes.STRONG_HEALING, predicateFalseMorelFermented, PotionTypes.STRONG_HARMING);
		PotionHelper.registerPotionTypeConversion(PotionTypes.POISON, predicateFalseMorelFermented, PotionTypes.HARMING);
		PotionHelper.registerPotionTypeConversion(PotionTypes.LONG_POISON, predicateFalseMorelFermented, PotionTypes.HARMING);
		PotionHelper.registerPotionTypeConversion(PotionTypes.STRONG_POISON, predicateFalseMorelFermented, PotionTypes.STRONG_HARMING);
		PotionHelper.registerPotionTypeConversion(PotionTypes.WATER, predicateFalseMorelFermented, PotionTypes.WEAKNESS);

		DropSpecifier dontDropWhenSilkTouching = new DropSpecifier(new ItemStack(falseMorel), 0.15f)
		{
			@Override
			public boolean shouldDrop(EntityPlayer harvester, int fortuneLevel, boolean isSilkTouching)
			{
				return !isSilkTouching && super.shouldDrop(harvester, fortuneLevel, isSilkTouching);
			}
		};
		Modifiers.drops.addDropsToBlock(new BlockSpecifier(Blocks.MYCELIUM.getDefaultState()), dontDropWhenSilkTouching);

		GameRegistry.addShapelessRecipe(new ItemStack(falseMorelFermented), new ItemStack(falseMorel), new ItemStack(Blocks.BROWN_MUSHROOM), new ItemStack(Items.SUGAR));
	}

	@Override
	public void finish()
	{
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void clientSidePost()
	{
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void clientSidePre()
	{
		ContentHelper.registerTypicalItemModel(falseMorel);
		ContentHelper.registerTypicalItemModel(falseMorelFermented);
	}
}

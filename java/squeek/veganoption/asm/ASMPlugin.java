package squeek.veganoption.asm;

import org.apache.logging.log4j.*;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.11.2")
@IFMLLoadingPlugin.TransformerExclusions("squeek.veganoption.asm")
public class ASMPlugin implements IFMLLoadingPlugin
{
	private static final Logger Log = LogManager.getLogger(ClassTransformer.class.getCanonicalName());

	@Override
	public String[] getASMTransformerClass()
	{
		Log.log(squeek.veganoption.ModInfo.debugLevel, "getASMTransformerClass() called.");
		return new String[]{ClassTransformer.class.getName()};
	}

	@Override
	public String getModContainerClass()
	{
		return null;
	}

	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
		Log.log(squeek.veganoption.ModInfo.debugLevel, "injectData(Map<String, Object> data) called.");
		ClassTransformer.isEnvObfuscated = (Boolean) data.get("runtimeDeobfuscationEnabled");
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}

}

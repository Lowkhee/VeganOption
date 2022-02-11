package squeek.veganoption.helpers;

//import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.translation.I18n;

import java.util.IllegalFormatException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import squeek.veganoption.ModInfo;

@SuppressWarnings("deprecation")
public class LangHelper
{
	private static final Logger Log = LogManager.getLogger(LangHelper.class.getCanonicalName());
	
	public LangHelper(){}
	
	public static String prependModId(String identifier)
	{
		return ModInfo.MODID + "." + identifier;
	}

	public static String translate(String identifier)
	{
		return translateRaw(prependModId(identifier));
	}

	public static String translate(String identifier, Object... args)
	{
		return translateRaw(prependModId(identifier), args);
	}

	public static boolean exists(String identifier)
	{
		return existsRaw(prependModId(identifier));
	}

	public static String translateRaw(String key)
	{
		return net.minecraft.client.resources.I18n.format(key);
	}

	public static String translateRaw(String key, Object... args)
	{
		return net.minecraft.client.resources.I18n.format(key, args);
	}

	public static boolean existsRaw(String key)
	{
		return net.minecraft.client.resources.I18n.hasKey(key);
	}

	public static String contextString(String format, String context, Object... params)
	{
		return translate(format + ".format", translate("context." + context + ".title", params), translate("context." + context + ".value", params), params);
	}

	public static String translateToLocal(String key) 
	{
		if (I18n.canTranslate(key)) 
			return I18n.translateToLocal(key); 
		else 
			return I18n.translateToFallback(key);
	}

	public static boolean canTranslateToLocal(String key) 
	{
			return I18n.canTranslate(key);
	}

	public static String translateToLocalFormatted(String key, Object... format) 
	{
		String formatKey = translateToLocal(key);
		try 
		{
				return String.format(formatKey, format);
		}
		catch (IllegalFormatException exception)
		{
				String errorMessage = "Format error: " + formatKey;
				Log.log(ModInfo.debugLevel, errorMessage, exception);
				return errorMessage;
		}
	}
}

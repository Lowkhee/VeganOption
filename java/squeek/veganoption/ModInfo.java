package squeek.veganoption;

import org.apache.logging.log4j.Level;

//import java.util.Locale;

public final class ModInfo
{
	public static final String MODNAME = "VeganOption"; //"${mod_name}";
	public static final String MODID = "veganoption"; //"${mod_id}";
	//public static final String MODID = MODID.toLowerCase(Locale.ROOT);
	public static final String MODVERSION = "1.1.0"; //"${mod_version}";
	public static final String MCVERSION = "1.11.2"; //"${required_mc_version}";
	public static final String FORGEVERSION = "13.20.1.2386"; //"${required_forge_version}";
	//public static final String JEIVERSION = "${required_jei_version}";
	//public static final String WAILAVERSION = "${required_waila_version}";
	//public static final String THEONEPROBEVERSION = "${required_theoneprobe_version}";
	//necessary? "after:*"  
	//public static final String DEPENDENCIES = "required-after:forge@[" + FORGEVERSION + ",);" + "after:jei@[" + JEIVERSION + ",);" 
	//										+ "after:waila@[" + WAILAVERSION + ",);" + "after:theoneprobe@[" + THEONEPROBEVERSION + ",)";
	//public static final String APIVERSION = "0.1.0";
	//public static final String APIPROVIDES = "VeganOptionAPI";
	/*Log.error("ERROR");
	Log.info("INFO");
	Log.fatal("FATAL");
	Log.warn("WARN");*/
	//debug
	public static final Level debugLevel = Level.TRACE; //set to DEBUG/TRACE to hide - INFO to Show
}

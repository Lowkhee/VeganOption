// Setting the owner to VeganOption creates a circular dependency do to "after:*" in VO's @Mod
// TODO: Figure out how to handle this best, or why that circular dependency can't be resolved
// for now, I guess just provide ourselves  -- VeganOptionAPI necessary? "after:*"
@API(apiVersion = "0.1.0", owner = "veganoption", provides = "veganoptionapi")
package squeek.veganoption.api;

import net.minecraftforge.fml.common.API;
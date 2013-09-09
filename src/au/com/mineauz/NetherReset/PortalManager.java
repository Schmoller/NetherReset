package au.com.mineauz.NetherReset;

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class PortalManager
{
	private HashMap<Block, Portal> mPortals = new HashMap<Block, Portal>();
	
	public void initialize() throws IOException
	{
		
	}
	
	/**
	 * Finds the portal object associated with that block, or creates a new one
	 * @param portalBlock 
	 * @return
	 */
	public Portal getPortal(Block portalBlock)
	{
		Validate.isTrue(portalBlock.getType() == Material.PORTAL, "This is not a portal");
		
		portalBlock = PortalHelper.findBaseBlock(portalBlock, PortalHelper.isPortalNS(portalBlock));

		if(!mPortals.containsKey(portalBlock))
			mPortals.put(portalBlock, new Portal(portalBlock));
		
		return mPortals.get(portalBlock);
	}
	
	public void registerPortal(Portal portal)
	{
		mPortals.put(portal.getBlock(), portal);
	}
}

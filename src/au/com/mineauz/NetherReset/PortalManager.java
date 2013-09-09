package au.com.mineauz.NetherReset;

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import au.com.mineauz.NetherReset.utilities.AgeingMap;

public class PortalManager
{
	private HashMap<Block, Portal> mPortals = new HashMap<Block, Portal>();
	private AgeingMap<Entity, Portal> mPortalBlackList;
	
	public PortalManager()
	{
		mPortalBlackList = new AgeingMap<Entity, Portal>(1000);
	}
	
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
	
	public boolean canUsePortal(Entity entity, Portal portal)
	{
		Portal blacklisted = mPortalBlackList.get(entity);
		return blacklisted != portal;
	}
	
	/**
	 * Force uses a portal, check with canUsePortal if you want to know
	 */
	public void usePortal(Entity entity, Portal portal)
	{
		Portal other = portal.getOrSpawnLinkedPortal();
		Location loc = other.getSpawnLocation(entity.getLocation());
		entity.teleport(loc);
		mPortalBlackList.put(entity, portal);
	}
}

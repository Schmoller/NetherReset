package au.com.mineauz.NetherReset;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;

import au.com.mineauz.NetherReset.utilities.AgeingMap;

public class PortalManager
{
	private HashMap<Block, Portal> mPortals = new HashMap<Block, Portal>();
	private AgeingMap<Entity, Portal> mPortalBlackList;
	
	private File mFile;
	
	public PortalManager()
	{
		mPortalBlackList = new AgeingMap<Entity, Portal>(1000);
	}
	
	public void initialize(File file) throws IOException
	{
		mFile = file;
		load();
	}
	
	private void load()
	{
		FileConfiguration config = new YamlConfiguration();
		
		if(!mFile.exists())
			return;

		mPortals.clear();
		
		HashMap<Portal, Location> links = new HashMap<Portal, Location>();
		
		try
		{
			config.load(mFile);
			
			for(String key : config.getKeys(false))
			{
				ConfigurationSection section = config.getConfigurationSection(key);
				Portal portal = new Portal(section);
				mPortals.put(portal.getBlock(), portal);
				
				if(section.contains("linked"))
					links.put(portal, Portal.getLocationFromString(section.getString("linked")));
				
				NetherReset.logger.info(portal.toString());
			}
			
			// Apply links
			for(Entry<Portal, Location> link : links.entrySet())
			{
				Portal other = mPortals.get(link.getValue().getBlock());
				link.getKey().setLinkedPortal(other);
				
				NetherReset.logger.info("Linked " + link.getKey().getLocationString() + " to " + other.getLocationString());
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public void save()
	{
		try
		{
			FileConfiguration config = new YamlConfiguration();
			for(Portal portal : mPortals.values())
			{
				ConfigurationSection section = config.createSection(portal.getLocationString());
				section.set("ns", portal.isNorthSouth());
				if(portal.getLinkedPortal() != null)
					section.set("linked", portal.getLinkedPortal().getLocationString());
			}
			
			config.save(mFile);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
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
		save();
	}
	
	public void removePortal(Portal portal)
	{
		mPortals.remove(portal.getBlock());
		save();
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
		final Portal other = portal.getOrSpawnLinkedPortal();
		
		Bukkit.getScheduler().runTaskLater(NetherReset.instance, new Runnable()
		{
			@Override
			public void run()
			{
				PortalHelper.applyPortal(other);
			}
		}, 2L);
		
		
		Location loc = other.getSpawnLocation(entity.getLocation());
		entity.teleport(loc);
		
		mPortalBlackList.put(entity, portal);
	}
	
	public void clearPortals()
	{
		mPortals.clear();
		save();
	}
}

package au.com.mineauz.NetherReset;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

public class Portal
{
	private Portal mLinked;
	
	private Block mBaseBlock;
	private boolean mNorthSouth;
	
	public Portal(Block portalBlock)
	{
		mLinked = null;
		
		mNorthSouth = PortalHelper.isPortalNS(portalBlock);
		
		// Find the base block
		mBaseBlock = PortalHelper.findBaseBlock(portalBlock, mNorthSouth);
	}
	
	public Portal(Block portalBlock, boolean ns)
	{
		mLinked = null;
		
		mNorthSouth = ns;
		
		// Find the base block
		mBaseBlock = PortalHelper.findBaseBlock(portalBlock, mNorthSouth);
	}
	
	public Portal(ConfigurationSection section)
	{
		mBaseBlock = getLocationFromString(section.getName()).getBlock();
		mNorthSouth = section.getBoolean("ns");
	}
	
	public Block getBlock()
	{
		return mBaseBlock;
	}
	
	public boolean isNorthSouth()
	{
		return mNorthSouth;
	}
	
	public Portal getLinkedPortal()
	{
		return mLinked;
	}
	
	public boolean exists()
	{
		// Check that the frame still exists
		if(mNorthSouth)
		{
			for(int z = mBaseBlock.getZ() - 2; z <= mBaseBlock.getZ() + 1; ++z)
			{
				for(int y = mBaseBlock.getY() - 1; y <= mBaseBlock.getY() + 3; ++y)
				{
					if((z == mBaseBlock.getZ() - 2) ^ (z == mBaseBlock.getZ() + 1) ^ (y == mBaseBlock.getY() - 1) ^ (y == mBaseBlock.getY() + 3))
					{
						if(mBaseBlock.getWorld().getBlockAt(mBaseBlock.getX(), y, z).getType() != Material.OBSIDIAN)
							return false;
					}
				}
			}
		}
		else
		{
			for(int x = mBaseBlock.getX() - 1; x <= mBaseBlock.getX() + 2; ++x)
			{
				for(int y = mBaseBlock.getY() - 1; y <= mBaseBlock.getY() + 3; ++y)
				{
					if((x == mBaseBlock.getX() - 1) ^ (x == mBaseBlock.getX() + 2) ^ (y == mBaseBlock.getY() - 1) ^ (y == mBaseBlock.getY() + 3))
					{
						if(mBaseBlock.getWorld().getBlockAt(x, y, mBaseBlock.getZ()).getType() != Material.OBSIDIAN)
							return false;
					}
				}
			}
		}
		
		return true;
	}
	
	public Portal getOrSpawnLinkedPortal()
	{
		if(mLinked != null && mLinked.exists())
			return mLinked;

		if(mLinked != null)
			NetherReset.logger.info("Frame does not exist: " + mLinked);
		else
			NetherReset.logger.info("No linked portal");
		Location coords;
		
		if(mBaseBlock.getWorld().equals(NetherReset.getNether()))
			coords = new Location(Bukkit.getWorlds().get(0), mBaseBlock.getX() * 8, mBaseBlock.getY(), mBaseBlock.getZ() * 8);
		else
			coords = new Location(NetherReset.getNether(), mBaseBlock.getX() / 8, mBaseBlock.getY(), mBaseBlock.getZ() / 8);
		
		mLinked = PortalHelper.createPortal(coords, mNorthSouth);
		mLinked.setLinkedPortal(this);
		
		NetherReset.getPortalManager().registerPortal(mLinked);
		
		return mLinked;
	}
	
	public void setLinkedPortal(Portal portal)
	{
		mLinked = portal;
	}
	
	public Location getSpawnLocation(Location source)
	{
		Location loc = mBaseBlock.getLocation().add((!mNorthSouth ? 1 : 0.5), 0, (!mNorthSouth ? 0.5 : 0));
		loc.setYaw(source.getYaw());
		loc.setPitch(source.getPitch());
		
		return loc;
	}
	
	public String getLocationString()
	{
		return getBlock().getWorld().getName() + ":" + getBlock().getX() + ":" + getBlock().getY() + ":" + getBlock().getZ();
	}
	
	public static Location getLocationFromString(String str)
	{
		String[] parts = str.split("\\:");
		
		return new Location(Bukkit.getWorld(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
	}
	
	@Override
	public String toString()
	{
		return String.format("Portal: {%s ns: %s}", getLocationString(), mNorthSouth);
	}
	
	@Override
	public boolean equals( Object obj )
	{
		if(!(obj instanceof Portal))
			return false;
		
		Portal p = (Portal)obj;
		
		return (p.mNorthSouth && p.mBaseBlock.equals(mBaseBlock));
	}
}

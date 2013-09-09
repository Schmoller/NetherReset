package au.com.mineauz.NetherReset;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class PortalHelper
{
	public static boolean isPortalNS(Block block)
	{
		return (block.getRelative(BlockFace.SOUTH).getType() == Material.PORTAL || block.getRelative(BlockFace.NORTH).getType() == Material.PORTAL);
	}
	public static Block findBaseBlock(Block start, boolean ns)
	{
		Block block = start;
		if(ns)
		{
			while(block.getRelative(BlockFace.SOUTH).getType() == Material.PORTAL)
				block = block.getRelative(BlockFace.SOUTH);

			while(block.getRelative(BlockFace.DOWN).getType() == Material.PORTAL)
				block = block.getRelative(BlockFace.DOWN);
		}
		else
		{
			while(block.getRelative(BlockFace.WEST).getType() == Material.PORTAL)
				block = block.getRelative(BlockFace.WEST);

			while(block.getRelative(BlockFace.DOWN).getType() == Material.PORTAL)
				block = block.getRelative(BlockFace.DOWN);
		}
		
		return block;
	}
	
	public static boolean canPlacePortal(Location location, boolean ns)
	{
		if(ns)
		{
			for(int z = location.getBlockZ(); z <= location.getBlockZ() + 1; ++z)
			{
				for(int y = location.getBlockY(); y <= location.getBlockY() + 2; ++y)
				{
					for(int x = location.getBlockX() - 1; x <= location.getBlockX() + 1; ++x)
					{
						if(!location.getWorld().getBlockAt(x, y, z).isEmpty())
							return false;
					}
				}
			}
		}
		else
		{
			for(int x = location.getBlockX(); x <= location.getBlockX() + 1; ++x)
			{
				for(int y = location.getBlockY(); y <= location.getBlockY() + 2; ++y)
				{
					for(int z = location.getBlockZ() - 1; z <= location.getBlockZ() + 1; ++z)
					{
						if(!location.getWorld().getBlockAt(x, y, z).isEmpty())
							return false;
					}
				}
			}
		}
		
		return true;
	}
	public static Portal createPortal(Location location, boolean ns)
	{
		// Find an empty position to put it.
		// Requirements: The actual portal part must be clear of blocks, as must the space in front and behind the actual portal part
		// If this is not met, try going up. NOTE: must not go up above 100 but only go up to 30 above the target position
		// If going up didnt succeede, try offsetting the location 
		
		boolean canPlace = false;
		outer: for(int i = 0; i < 60; ++i)
		{
			// First we will start at the exact location, then we step along the positive dir, then jump to the negative dir
			int offset = (i <= 30 ? i : 30 - (i - 30));
			
			for(int y = location.getBlockY(); y < location.getBlockY() + 30 && y < 100; ++y)
			{
				Location loc = new Location(location.getWorld(), location.getX() + (ns ? 0 : offset), y, location.getZ() + (ns ? offset : 0));
				if(canPlacePortal(loc, ns))
				{
					canPlace = true;
					location = loc;
					break outer;
				}
			}
		}
		
		if(!canPlace)
		{
			// This time we will go along the other axis
			outer2: for(int i = 0; i < 60; ++i)
			{
				// First we will start at the exact location, then we step along the positive dir, then jump to the negative dir
				int offset = (i <= 30 ? i : 30 - (i - 30));
				
				for(int y = location.getBlockY(); y < location.getBlockY() + 30 && y < 100; ++y)
				{
					Location loc = new Location(location.getWorld(), location.getX() + (ns ? offset : 0), y, location.getZ() + (ns ? 0 : offset));
					if(canPlacePortal(loc, ns))
					{
						canPlace = true;
						location = loc;
						break outer2;
					}
				}
			}
		}
		
		if(!canPlace)
		{
			// Generate a void around the portal
			for(int y = location.getBlockY(); y <= location.getBlockY() + 6; ++y)
			{
				if(ns)
				{
					for(int x = location.getBlockX() - 2; x <= location.getBlockX() + 2; ++x)
					{
						for(int z = location.getBlockZ() - 3; z <= location.getBlockZ() + 4; ++z)
						{
							location.getWorld().getBlockAt(x, y, z).setTypeId(0, false);
						}
					}
				}
				else
				{
					for(int x = location.getBlockX() - 3; x <= location.getBlockX() + 4; ++x)
					{
						for(int z = location.getBlockZ() - 2; z <= location.getBlockZ() + 2; ++z)
						{
							location.getWorld().getBlockAt(x, y, z).setTypeId(0, false);
						}
					}
				}
			}
			
		}
		
		// Make the actual portal
		if(ns)
		{
			// Build the frame first
			for(int z = location.getBlockZ() - 1; z <= location.getBlockZ() + 2; ++z)
			{
				for(int y = location.getBlockY() - 1; y <= location.getBlockY() + 3; ++y)
				{
					if(z == location.getBlockZ() - 1 || z == location.getBlockZ() + 2 || y == location.getBlockY() - 1 || y == location.getBlockY() + 3)
						location.getWorld().getBlockAt(location.getBlockX(), y, z).setType(Material.OBSIDIAN);
				}
			}
			
			// Build the portal
			for(int z = location.getBlockZ(); z <= location.getBlockZ() + 1; ++z)
			{
				for(int y = location.getBlockY(); y <= location.getBlockY() + 2; ++y)
				{
					location.getWorld().getBlockAt(location.getBlockX(), y, z).setTypeId(Material.PORTAL.getId(), false);
				}
			}
		}
		else
		{
			// Build the frame first
			for(int x = location.getBlockX() - 1; x <= location.getBlockX() + 2; ++x)
			{
				for(int y = location.getBlockY() - 1; y <= location.getBlockY() + 3; ++y)
				{
					if(x == location.getBlockX() - 1 || x == location.getBlockX() + 2 || y == location.getBlockY() - 1 || y == location.getBlockY() + 3)
						location.getWorld().getBlockAt(x, y, location.getBlockZ()).setType(Material.OBSIDIAN);
				}
			}
			
			// Build the portal
			for(int x = location.getBlockX(); x <= location.getBlockX() + 1; ++x)
			{
				for(int y = location.getBlockY(); y <= location.getBlockY() + 2; ++y)
				{
					location.getWorld().getBlockAt(x, y, location.getBlockZ()).setTypeId(Material.PORTAL.getId(), false);
				}
			}
		}
		
		return new Portal(location.getBlock());
	}
}

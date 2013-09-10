package au.com.mineauz.NetherReset;

import org.bukkit.Bukkit;
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
			for(int z = location.getBlockZ()-1; z <= location.getBlockZ() + 2; ++z)
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
			for(int x = location.getBlockX() - 1; x <= location.getBlockX() + 2; ++x)
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
		
		if(location.getBlock().getRelative(BlockFace.DOWN).isEmpty())
			return false;
		
		return true;
	}
	
	public static void applyPortal(Portal portal)
	{
		if(portal.isNorthSouth())
		{
			for(int z = portal.getBlock().getZ() - 1; z <= portal.getBlock().getZ(); ++z)
			{
				for(int y = portal.getBlock().getY(); y <= portal.getBlock().getY() + 2; ++y)
				{
					portal.getBlock().getWorld().getBlockAt(portal.getBlock().getX(), y, z).setTypeId(Material.PORTAL.getId(), false);
				}
			}
		}
		else
		{
			for(int x = portal.getBlock().getX(); x <= portal.getBlock().getX() + 1; ++x)
			{
				for(int y = portal.getBlock().getY(); y <= portal.getBlock().getY() + 2; ++y)
				{
					portal.getBlock().getWorld().getBlockAt(x, y, portal.getBlock().getZ()).setTypeId(Material.PORTAL.getId(), false);
				}
			}
		}
	}
	
	public static Portal createPortal(Location location, boolean ns)
	{
		// Find an empty position to put it.
		// Requirements: The actual portal part must be clear of blocks, as must the space in front and behind the actual portal part
		// If this is not met, try going up. NOTE: must not go up above 100 but only go up to 30 above the target position
		// If going up didnt succeede, try offsetting the location 
		
		Location best = null;
		double bestDist = Double.MAX_VALUE;
		Location bestAnti = null;
		double bestAntiDist = Double.MAX_VALUE;
		
		for(int x = location.getBlockX() - 16; x < location.getBlockX() + 16; ++x)
		{
			for(int z = location.getBlockZ() - 16; z < location.getBlockZ() + 16; ++z)
			{
				for(int y = 5; y < 110; ++y)
				{
					Location loc = new Location(location.getWorld(), x, y, z);
					double dist = loc.distanceSquared(location); 
					
					if(dist < bestDist)
					{
						if(canPlacePortal(loc, ns))
						{
							best = loc;
							bestDist = dist;
						}
						else 
						{
							if(dist < bestAntiDist)
							{
								if(canPlacePortal(loc, !ns))
								{
									bestAnti = loc;
									bestAntiDist = dist;
								}
							}
						}
					}
				}
			}
		}

		boolean force = false;
		
		if(best != null)
			location = best;
		else if(bestAnti != null)
		{
			location = bestAnti;
			ns = !ns;
		}
		else
			force = true;
		
		final Location fLocation = location;
		final boolean fForce = force;
		final boolean fNS = ns;
		
		Bukkit.getScheduler().runTaskLater(NetherReset.instance, new Runnable()
		{
			@Override
			public void run()
			{
				if(fForce)
				{
					// Generate a void around the portal
					for(int y = fLocation.getBlockY(); y <= fLocation.getBlockY() + 6; ++y)
					{
						if(fNS)
						{
							for(int x = fLocation.getBlockX() - 2; x <= fLocation.getBlockX() + 2; ++x)
							{
								for(int z = fLocation.getBlockZ() - 3; z <= fLocation.getBlockZ() + 4; ++z)
								{
									fLocation.getWorld().getBlockAt(x, y, z).setTypeId(0, false);
								}
							}
						}
						else
						{
							for(int x = fLocation.getBlockX() - 3; x <= fLocation.getBlockX() + 4; ++x)
							{
								for(int z = fLocation.getBlockZ() - 2; z <= fLocation.getBlockZ() + 2; ++z)
								{
									fLocation.getWorld().getBlockAt(x, y, z).setTypeId(0, false);
								}
							}
						}
					}
					
				}
				
				// Make the actual portal
				if(fNS)
				{
					int c = 0;

					// Build the frame first
					boolean retry = true;
					// Found a case where frame doesnt fully generate.
					// Heres what i know:
					// setTypeId works, completly in every case
					// but it doesnt at the same time. I find that there are two instances of the chunk, one that setType uses, and one that getType uses.
					// Somehow I dont think this is intensional, but that is how it is.
					// For the ones that work, the instances are the same.

					while(retry)
					{
						retry = false;
						for(int z = fLocation.getBlockZ() - 2; z <= fLocation.getBlockZ() + 1; ++z)
						{
							for(int y = fLocation.getBlockY() - 1; y <= fLocation.getBlockY() + 3; ++y)
							{
								if(z == fLocation.getBlockZ() - 2 || z == fLocation.getBlockZ() + 1 || y == fLocation.getBlockY() - 1 || y == fLocation.getBlockY() + 3)
								{
									Block b = fLocation.getWorld().getBlockAt(fLocation.getBlockX(), y, z);
									b.setType(Material.OBSIDIAN);
									
									if(b.getType() != Material.OBSIDIAN)
										retry = true;
									++c;
								}
							}
						}
					}
					NetherReset.logger.info("blocks " + c);
					
					Block base = fLocation.getBlock().getRelative(BlockFace.DOWN);
					
					if(fForce)
					{
						base.getRelative(BlockFace.WEST).setType(Material.OBSIDIAN);
						base.getRelative(BlockFace.EAST).setType(Material.OBSIDIAN);
						base.getRelative(BlockFace.WEST).getRelative(BlockFace.NORTH).setType(Material.OBSIDIAN);
						base.getRelative(BlockFace.EAST).getRelative(BlockFace.NORTH).setType(Material.OBSIDIAN);
					}
				}
				else
				{
					boolean retry = true;
					while(retry)
					{
						retry = false;
						// Build the frame first
						for(int x = fLocation.getBlockX() - 1; x <= fLocation.getBlockX() + 2; ++x)
						{
							for(int y = fLocation.getBlockY() - 1; y <= fLocation.getBlockY() + 3; ++y)
							{
								if(x == fLocation.getBlockX() - 1 || x == fLocation.getBlockX() + 2 || y == fLocation.getBlockY() - 1 || y == fLocation.getBlockY() + 3)
								{
									Block b = fLocation.getWorld().getBlockAt(x, y, fLocation.getBlockZ());
									b.setType(Material.OBSIDIAN);
									
									if(b.getType() != Material.OBSIDIAN)
										retry = true;
								}
							}
						}
					}
					
					Block base = fLocation.getBlock().getRelative(BlockFace.DOWN);
					
					if(fForce)
					{
						base.getRelative(BlockFace.SOUTH).setType(Material.OBSIDIAN);
						base.getRelative(BlockFace.NORTH).setType(Material.OBSIDIAN);
						base.getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST).setType(Material.OBSIDIAN);
						base.getRelative(BlockFace.NORTH).getRelative(BlockFace.EAST).setType(Material.OBSIDIAN);
					}
				}
			}
		}, 2L);
		
		
		return new Portal(location.getBlock(), ns);
	}
}

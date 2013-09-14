package au.com.mineauz.NetherReset;

import java.util.ArrayList;

import net.minecraft.server.v1_6_R2.EntityItem;
import net.minecraft.server.v1_6_R2.EntityMinecartAbstract;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.PortalCreateEvent.CreateReason;

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
	
	private static void setIfNotSolid(Block block, Material mat)
	{
		if(!block.getType().isSolid())
			block.setType(mat);
	}
	
	private static void internalCreatePortal(Location location, boolean ns, boolean force)
	{
		if(force)
		{
			// Generate a void around the portal
			for(int y = location.getBlockY(); y <= location.getBlockY() + 4; ++y)
			{
				if(ns)
				{
					for(int x = location.getBlockX() - 2; x <= location.getBlockX() + 2; ++x)
					{
						for(int z = location.getBlockZ() - 3; z <= location.getBlockZ() + 2; ++z)
						{
							location.getWorld().getBlockAt(x, y, z).setTypeId(0, false);
						}
					}
				}
				else
				{
					for(int x = location.getBlockX() - 2; x <= location.getBlockX() + 3; ++x)
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
			// Found a case where frame doesnt fully generate.
			// Heres what i know:
			// setTypeId works, completly in every case
			// but it doesnt at the same time. I find that there are two instances of the chunk, one that setType uses, and one that getType uses.
			// Somehow I dont think this is intensional, but that is how it is.
			// For the ones that work, the instances are the same.

			for(int z = location.getBlockZ() - 2; z <= location.getBlockZ() + 1; ++z)
			{
				for(int y = location.getBlockY() - 1; y <= location.getBlockY() + 3; ++y)
				{
					if(z == location.getBlockZ() - 2 || z == location.getBlockZ() + 1 || y == location.getBlockY() - 1 || y == location.getBlockY() + 3)
					{
						Block b = location.getWorld().getBlockAt(location.getBlockX(), y, z);
						b.setType(Material.OBSIDIAN);
					}
				}
			}

			Block base = location.getBlock().getRelative(BlockFace.DOWN);
			
			setIfNotSolid(base.getRelative(BlockFace.WEST),Material.OBSIDIAN);
			setIfNotSolid(base.getRelative(BlockFace.EAST),Material.OBSIDIAN);
			setIfNotSolid(base.getRelative(BlockFace.WEST).getRelative(BlockFace.NORTH),Material.OBSIDIAN);
			setIfNotSolid(base.getRelative(BlockFace.EAST).getRelative(BlockFace.NORTH),Material.OBSIDIAN);
		}
		else
		{
			// Build the frame first
			for(int x = location.getBlockX() - 1; x <= location.getBlockX() + 2; ++x)
			{
				for(int y = location.getBlockY() - 1; y <= location.getBlockY() + 3; ++y)
				{
					if(x == location.getBlockX() - 1 || x == location.getBlockX() + 2 || y == location.getBlockY() - 1 || y == location.getBlockY() + 3)
					{
						Block b = location.getWorld().getBlockAt(x, y, location.getBlockZ());
						b.setType(Material.OBSIDIAN);
					}
				}
			}
			
			Block base = location.getBlock().getRelative(BlockFace.DOWN);
			
			setIfNotSolid(base.getRelative(BlockFace.SOUTH),Material.OBSIDIAN);
			setIfNotSolid(base.getRelative(BlockFace.NORTH),Material.OBSIDIAN);
			setIfNotSolid(base.getRelative(BlockFace.SOUTH).getRelative(BlockFace.EAST),Material.OBSIDIAN);
			setIfNotSolid(base.getRelative(BlockFace.NORTH).getRelative(BlockFace.EAST),Material.OBSIDIAN);
		}
	}
	
	public static Portal createPortal(Location location, boolean ns)
	{
		Location best = null;
		double bestDist = Double.MAX_VALUE;
		Location bestAnti = null;
		double bestAntiDist = Double.MAX_VALUE;
		
		int maxY = (location.getWorld().getEnvironment() == Environment.NORMAL ? 240 : 110);
		
		for(int x = location.getBlockX() - 16; x < location.getBlockX() + 16; ++x)
		{
			for(int z = location.getBlockZ() - 16; z < location.getBlockZ() + 16; ++z)
			{
				for(int y = 5; y < maxY; ++y)
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
		{
			force = true;
			location.setY(Math.min(Math.max(location.getY(), 10), 100));
		}
		
		ArrayList<Block> blocks = new ArrayList<Block>();
		for(int i = 0; i < 2; ++i)
		{
			for(int j = 0; j < 3; ++j)
				blocks.add(location.getBlock().getRelative(ns ? 0 : i, j, ns ? i : 0));
		}
		
		PortalCreateEvent event = new PortalCreateEvent(blocks, location.getWorld(), CreateReason.OBC_DESTINATION);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled())
			return null;
		
		if(force)
			internalCreatePortal(location, ns, true);
		else
		{
			final Location fLocation = location;
			final boolean fNS = ns;
			Bukkit.getScheduler().runTaskLater(NetherReset.instance, new Runnable()
			{
				@Override
				public void run()
				{
					internalCreatePortal(fLocation, fNS, false);
				}
			}, 2L);
		}
		
		return new Portal(location.getBlock(), ns);
	}
	
	public static void setDefaultCooldown(Entity ent)
	{
		((CraftEntity)ent).getHandle().portalCooldown = ((CraftEntity)ent).getHandle().ab();
	}
	public static void setCooldown(Entity ent, int time)
	{
		((CraftEntity)ent).getHandle().portalCooldown = time;
	}
	
	public static boolean isCooldownComplete(Entity ent)
	{
		return ((CraftEntity)ent).getHandle().portalCooldown <= 0;
	}
	
	public static boolean teleportEntity(Entity ent, Location dest)
	{
		return teleportEntity(((CraftEntity)ent).getHandle(), dest);
	}
	public static boolean teleportEntity(final net.minecraft.server.v1_6_R2.Entity entity, Location dest)
	{
		if(!dest.getChunk().load())
			Bukkit.broadcastMessage("Didnt load");
		
		WorldServer newworld = ((CraftWorld)dest.getWorld()).getHandle();
        if (entity.world != newworld && !(entity instanceof EntityPlayer)) 
        {   
            if (entity.passenger != null) 
            {
                final net.minecraft.server.v1_6_R2.Entity passenger = entity.passenger;
                passenger.vehicle = null;
                entity.passenger = null;
                if (teleportEntity(passenger, dest)) 
                {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(NetherReset.instance, new Runnable() 
                    {
                        public void run() 
                        {
                            passenger.setPassengerOf(entity);
                        }
                    });
                }
            }
            
            entity.world.removeEntity(entity);
            entity.dead = false;
            
            boolean before = newworld.chunkProviderServer.forceChunkLoad;
            newworld.chunkProviderServer.forceChunkLoad = true;
            newworld.getMinecraftServer().getPlayerList().repositionEntity(entity, dest, false);
            newworld.chunkProviderServer.forceChunkLoad = before;
 
            if(entity instanceof EntityItem)
            {
            	((EntityItem)entity).pickupDelay = 0;
//            	Item copy = dest.getWorld().dropItem(dest, ((Item)entity.getBukkitEntity()).getItemStack());
//            	copy.setVelocity(entity.getBukkitEntity().getVelocity());
//            	entity.getBukkitEntity().remove();
//            	net.minecraft.server.v1_6_R2.Entity copyEnt = ((CraftItem)copy).getHandle();
//            	copyEnt.portalCooldown = entity.portalCooldown;
            }
            if(entity instanceof EntityMinecartAbstract)
            	dest.add(0, 1, 0);
//            ((WorldServer)entity.world).tracker.untrackEntity(entity);
//            entity.world.removeEntity(entity);
//            entity.dead = false;
//            entity.world = newworld;
//            entity.setLocation(dest.getX(), dest.getY(), dest.getZ(), dest.getYaw(), dest.getPitch());
//            
//            entity.world.addEntity(entity);
            
            //((WorldServer)entity.world).tracker.track(entity);
            return true;
        } 
        else 
            return entity.getBukkitEntity().teleport(dest);           
	}
}

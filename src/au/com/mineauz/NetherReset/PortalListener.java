package au.com.mineauz.NetherReset;

import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.scheduler.BukkitTask;

public class PortalListener implements Listener
{
	public static int portalDelay = 80;
	
	private WeakHashMap<Entity, BukkitTask> mWaiting = new WeakHashMap<Entity, BukkitTask>();
	
	@EventHandler(ignoreCancelled = true)
	private void onEnterPortal(EntityPortalEnterEvent event)
	{
		if(event.getEntity() instanceof Player)
			mWaiting.put(event.getEntity(), Bukkit.getScheduler().runTaskLater(NetherReset.instance, new PortalTimer(event.getEntity(), NetherReset.getPortalManager().getPortal(event.getLocation().getBlock())), portalDelay));
		else
			usePortal(event.getEntity(), NetherReset.getPortalManager().getPortal(event.getLocation().getBlock()));
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onExitPortal(EntityPortalExitEvent event)
	{
		BukkitTask task = mWaiting.remove(event.getEntity());
		if(task != null)
			task.cancel();
	}
	
	private static void usePortal(Entity entity, Portal portal)
	{
		Portal other = portal.getOrSpawnLinkedPortal();
		Location loc = other.getSpawnLocation(entity.getLocation());
		entity.teleport(loc);
	}
	
	private static class PortalTimer implements Runnable
	{
		private Entity mEntity;
		private Portal mPortal;
		
		public PortalTimer(Entity entity, Portal portal)
		{
			mEntity = entity;
			mPortal = portal;
		}
		
		@Override
		public void run()
		{
			usePortal(mEntity, mPortal);
		}
		
	}
}

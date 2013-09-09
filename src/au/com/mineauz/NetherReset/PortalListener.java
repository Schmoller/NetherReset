package au.com.mineauz.NetherReset;

import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

public class PortalListener implements Listener
{
	public static int portalDelay = 80;
	
	private WeakHashMap<Entity, BukkitTask> mWaiting = new WeakHashMap<Entity, BukkitTask>();
	private WeakHashMap<Entity, Portal> mInPortal = new WeakHashMap<Entity, Portal>();
	
	@EventHandler(ignoreCancelled = true)
	private void onEnterPortal(EntityPortalEnterEvent event)
	{
		if(mInPortal.containsKey(event.getEntity()))
			return;
		
		Portal portal = NetherReset.getPortalManager().getPortal(event.getLocation().getBlock());
		mInPortal.put(event.getEntity(), portal);
		
		System.out.println(event.getEntity() + " entered the portal");
		if(!NetherReset.getPortalManager().canUsePortal(event.getEntity(), portal))
			return;
		
		
		if(event.getEntity() instanceof Player)
			mWaiting.put(event.getEntity(), Bukkit.getScheduler().runTaskLater(NetherReset.instance, new PortalTimer(event.getEntity(), portal), portalDelay));
		else
			usePortal(event.getEntity(), portal);
	}
	
	@EventHandler(ignoreCancelled = true)
	private void test(EntityPortalExitEvent event)
	{
		System.out.println(event.getEntity() + " exited the portal");
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onExitPortal(PlayerMoveEvent event)
	{
		if(!mInPortal.containsKey(event.getPlayer()))
			return;
		
		Block block = event.getPlayer().getLocation().getBlock();
		if(block.getType() != Material.PORTAL)
		{
			System.out.println(event.getPlayer().getName() + " left the portal");
			mInPortal.remove(event.getPlayer());
			BukkitTask task = mWaiting.get(event.getPlayer());
			if(task != null)
				task.cancel();
		}
	}
	
	private static void usePortal(Entity entity, Portal portal)
	{
		if(NetherReset.getPortalManager().canUsePortal(entity, portal))
			NetherReset.getPortalManager().usePortal(entity, portal);
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

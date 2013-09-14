package au.com.mineauz.NetherReset;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.scheduler.BukkitTask;

import au.com.mineauz.NetherReset.utilities.AgeingMap;

public class PortalListener implements Listener
{
	public static int portalDelay = 80; // 4 seconds
	
	private AgeingMap<Entity, BukkitTask> mWaiting = new AgeingMap<Entity, BukkitTask>(10000);
	private AgeingMap<Entity, Portal> mInPortal = new AgeingMap<Entity, Portal>(10000);
	
	@EventHandler(ignoreCancelled = true)
	private void onEnterPortal(EntityPortalEnterEvent event)
	{
		if(!(event.getEntity() instanceof Player) || !PortalHelper.isCooldownComplete(event.getEntity()) || mInPortal.containsKey(event.getEntity()))
			return;

		Portal portal = NetherReset.getPortalManager().getPortal(event.getLocation().getBlock());
		mInPortal.put(event.getEntity(), portal);
		
		if(!((Player)event.getEntity()).hasPermission("resetNether.no-portal-delay"))
			mWaiting.put(event.getEntity(), Bukkit.getScheduler().runTaskLater(NetherReset.instance, new PortalTimer(event.getEntity(), portal), portalDelay));
		else
			NetherReset.getPortalManager().usePortal(event.getEntity(), portal);
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
			NetherReset.getPortalManager().usePortal(mEntity, mPortal);
		}
		
	}
}

package au.com.mineauz.NetherReset;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PortalTravelEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	private boolean mIsCancelled = false;
	
	private Portal mSource;
	private Portal mDest;
	private Entity mEnt;
	
	public PortalTravelEvent(Portal source, Portal dest, Entity entity)
	{
		mSource = source;
		mDest = dest;
		mEnt = entity;
	}
	
	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}

	@Override
	public boolean isCancelled()
	{
		return mIsCancelled;
	}

	@Override
	public void setCancelled( boolean cancel )
	{
		mIsCancelled = cancel;
	}

	public Portal getSource()
	{
		return mSource;
	}
	
	public Portal getDest()
	{
		return mDest;
	}
	
	public Entity getEntity()
	{
		return mEnt;
	}
}

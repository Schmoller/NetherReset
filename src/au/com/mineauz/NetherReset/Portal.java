package au.com.mineauz.NetherReset;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

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
	
	public Portal getOrSpawnLinkedPortal()
	{
		if(mLinked != null)
			return mLinked;
		
		Location coords;
		
		if(mBaseBlock.getWorld().equals(NetherReset.getNether()))
			coords = new Location(Bukkit.getWorlds().get(0), mBaseBlock.getX() * 8, mBaseBlock.getY(), mBaseBlock.getZ() * 8);
		else
			coords = new Location(NetherReset.getNether(), mBaseBlock.getX() / 8, mBaseBlock.getY(), mBaseBlock.getZ() / 8);
		
		mLinked = PortalHelper.createPortal(coords, mNorthSouth);
		
		NetherReset.getPortalManager().registerPortal(mLinked);
		
		return mLinked;
	}
	
	public Location getSpawnLocation(Location source)
	{
		Location loc = mBaseBlock.getLocation().add((!mNorthSouth ? 1 : 0.5), 0, (!mNorthSouth ? 0.5 : 1));
		loc.setYaw(source.getYaw());
		loc.setPitch(source.getPitch());
		
		return loc;
	}
}

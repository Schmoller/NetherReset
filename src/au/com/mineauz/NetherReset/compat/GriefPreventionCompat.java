package au.com.mineauz.NetherReset.compat;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import au.com.mineauz.NetherReset.PortalTravelEvent;

public class GriefPreventionCompat implements Listener
{
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPortalTravel(PortalTravelEvent event)
	{
		Player player = null;
		if(event.getEntity() instanceof Player)
			player = (Player)event.getEntity();
		else if(event.getEntity().getPassenger() instanceof Player)
			player = (Player)event.getEntity().getPassenger();
		else
			return;
		
		// Check both the source side, and the dest side
		PlayerData pdata = GriefPrevention.instance.dataStore.getPlayerData(player.getName());
		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(event.getSource().getSpawnLocation(player.getLocation()), true, pdata.lastClaim);
		
		if(claim != null)
		{
			String reason = claim.allowAccess(player);
			
			if(reason != null)
			{
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + reason);
				return;
			}
		}
		
		claim = GriefPrevention.instance.dataStore.getClaimAt(event.getDest().getSpawnLocation(player.getLocation()), true, pdata.lastClaim);
		
		if(claim != null)
		{
			String reason = claim.allowAccess(player);
			
			if(reason != null)
			{
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + reason);
			}
		}
	}
}

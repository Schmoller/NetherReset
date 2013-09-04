package au.com.mineauz.NetherReset;

import java.util.logging.Logger;

import net.minecraft.server.v1_6_R2.MinecraftServer;
import net.minecraft.server.v1_6_R2.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class NetherReset extends JavaPlugin implements Listener
{
	private boolean mNetherLockout = false;
	
	public static Logger logger;
	
	@Override
	public void onEnable()
	{
		logger = getLogger();
	}
	
	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
	{
		if(command.getName().equals("resetNether"))
		{
			beginNetherReset();
			
			return true;
		}
		
		return false;
	}
	
	
	private void beginNetherReset()
	{
		mNetherLockout = true;
		
		Bukkit.broadcastMessage(ChatColor.DARK_PURPLE +"=====================================================");
		Bukkit.broadcastMessage("");
		Bukkit.broadcastMessage(ChatColor.YELLOW +     "                       WARNING                       ");
		Bukkit.broadcastMessage("");
		Bukkit.broadcastMessage(ChatColor.YELLOW +     "          The nether is being regenerated.");
		Bukkit.broadcastMessage("");
		Bukkit.broadcastMessage(ChatColor.DARK_PURPLE +"=====================================================");
		
		World nether = Bukkit.getWorld("world_nether");
		
		// Get the safe spawn point
		Location spawnPoint = Bukkit.getWorlds().get(0).getSpawnLocation().clone();
		while(!spawnPoint.getBlock().isEmpty() || !spawnPoint.getBlock().getRelative(BlockFace.UP).isEmpty())
			spawnPoint.add(0,1,0);

		// Kick all players from the nether
		for(Player player : nether.getPlayers())
		{
			player.teleport(spawnPoint);
			player.sendMessage(ChatColor.RED + "[WARNING] " + ChatColor.WHITE + " The nether is being regenerated. You have been removed from the nether for your safety.");
		}
		
		
		Bukkit.getPluginManager().callEvent(new WorldUnloadEvent(nether));
		
		CraftWorld world = (CraftWorld)nether;
		MinecraftServer server = ((CraftServer)getServer()).getServer();
		WorldServer worldServer = world.getHandle();
	
		server.worlds.remove(server.worlds.indexOf(worldServer));
		server.worldServer[1] = null;
		
		TaskChain chain = new TaskChain(new WorldDeleteTask(nether, "uid.dat"), new WorldCreateTask(worldServer, server));
		Bukkit.getScheduler().runTaskLater(this, chain, 20L);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
	private void onPlayerTeleport(EntityTeleportEvent event)
	{
		if(mNetherLockout && event.getTo().getWorld().getName().equals("world_nether"))
			event.setCancelled(true);
	}

	@EventHandler
	private void onNetherLoad(WorldLoadEvent event)
	{
		if(mNetherLockout && event.getWorld().getName().equals("world_nether"))
		{
			mNetherLockout = false;
			Bukkit.broadcastMessage(ChatColor.RED + "[Nether Reset] " + ChatColor.WHITE + "Nether regeneration is complete. Access to the nether is available again.");
		}
	}
}

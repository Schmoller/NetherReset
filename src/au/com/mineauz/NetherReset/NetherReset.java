package au.com.mineauz.NetherReset;

import java.lang.ref.WeakReference;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class NetherReset extends JavaPlugin implements Listener
{
	private boolean mNetherLockout = false;
	
	public static Logger logger;
	public static NetherReset instance;
	
	private WeakReference<World> mNether;
	
	private PortalManager mPortals;
	
	public static PortalManager getPortalManager()
	{
		return instance.mPortals;
	}
	
	public static World getNether()
	{
		if(!instance.mNether.isEnqueued())
			return instance.mNether.get();
		return null;
	}
	
	@Override
	public void onEnable()
	{
		instance = this;
		logger = getLogger();
		
		if(getServer().getAllowNether())
		{
			getLogger().severe("Nether world needs to be disabled for this plugin to work");
			instance = null;
			return;
		}
		
		mPortals = new PortalManager();
		
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(new PortalListener(), this);
		
		createNewNether();
	}
	
	void createNewNether()
	{
		WorldCreator world = new WorldCreator("world_nether");
		world.seed(new Random().nextLong());
		world.environment(Environment.NETHER);
		world.generateStructures(true);
		world.type(WorldType.NORMAL);
		
		mNether = new WeakReference<World>(Bukkit.createWorld(world));
	}
	
	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
	{
		if(instance == null)
			return false;
		
		if(command.getName().equals("resetNether"))
		{
			beginNetherReset();
			
			return true;
		}
		else if(command.getName().equals("tpn"))
		{
			if(sender instanceof Player && !mNether.isEnqueued())
			{
				((Player)sender).teleport(mNether.get().getSpawnLocation());
			}
				
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
			player.sendMessage(ChatColor.RED + "[WARNING] " + ChatColor.WHITE + " You have been removed from the nether while it is regenerated.");
		}
		
		if(!Bukkit.unloadWorld(nether, false))
		{
			logger.severe("couldnt unload nether");
			return;
		}
		
		TaskChain chain = new TaskChain(new WorldDeleteTask(nether), new WorldCreateTask());
		Bukkit.getScheduler().runTaskLater(this, chain, 20L);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
	private void onPlayerTeleport(EntityTeleportEvent event)
	{
		if(mNetherLockout && event.getTo().getWorld().getName().equals("world_nether"))
			event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	private void onEntityPortalEnter(EntityPortalEnterEvent event)
	{
//		logger.info("Portal event");
//		if(event.getLocation().getWorld().equals(Bukkit.getWorlds().get(0)))
//		{
//			event.getEntity().teleport(mNether.get().getSpawnLocation());
//		}
	}
	


	void onNetherLoad()
	{
		if(mNetherLockout)
		{
			mNetherLockout = false;
			Bukkit.broadcastMessage(ChatColor.RED + "[Nether Reset] " + ChatColor.WHITE + "Nether regeneration is complete. Access to the nether is available again.");
		}
	}
	
	
}

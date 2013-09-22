package au.com.mineauz.NetherReset;

import java.io.File;
import java.io.IOException;
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
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import au.com.mineauz.NetherReset.compat.GriefPreventionCompat;

public class NetherReset extends JavaPlugin implements Listener
{
	private boolean mNetherLockout = false;
	
	public static Logger logger;
	public static NetherReset instance;
	
	private WeakReference<World> mNether;
	
	private PortalManager mPortals;
	
	private Config mConfig;
	
	public String getWorldName()
	{
		if(mConfig.world.isEmpty())
			return Bukkit.getWorlds().get(0).getName() + "_nether";
		else
			return mConfig.world;
	}
	
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
			setEnabled(false);
			return;
		}
		
		mConfig = new Config(new File(getDataFolder(), "config.yml"));
		if(!mConfig.load())
		{
			getLogger().severe("Error in configuration file. Cannot load NetherReset");
			instance = null;
			setEnabled(false);
			return;
		}
		
		loadExistingNether();
		
		mPortals = new PortalManager();
		try
		{
			mPortals.initialize(new File(getDataFolder(), "portals.yml"));
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			
		}
		
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(new PortalListener(), this);
		
		if(Bukkit.getPluginManager().isPluginEnabled("GriefPrevention"))
			Bukkit.getPluginManager().registerEvents(new GriefPreventionCompat(), this);
	}
	
	void createNewNether()
	{
		WorldCreator world = new WorldCreator(getWorldName());
		world.seed(new Random().nextLong());
		world.environment(Environment.NETHER);
		world.generateStructures(true);
		world.type(WorldType.NORMAL);
		
		mNether = new WeakReference<World>(Bukkit.createWorld(world));
		saveNetherInfo(world);
	}
	
	private void loadExistingNether()
	{
		File file = new File(getWorldName() + "/level.yml");
		if(!file.exists())
		{
			createNewNether();
			return;
		}
		
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.load(file);
			WorldCreator world = new WorldCreator(getWorldName());
			world.environment(Environment.NETHER);
			
			world.generateStructures(config.getBoolean("structures"));
			world.seed(config.getLong("seed"));
			world.type(WorldType.valueOf(config.getString("type")));
			
			mNether = new WeakReference<World>(Bukkit.createWorld(world));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(InvalidConfigurationException e)
		{
			getLogger().severe("Failed to load nether settings. Generating new settings. This may or may not cause chunk errors in the nether");
			createNewNether();
		}
	}
	
	private void saveNetherInfo(WorldCreator worldInfo)
	{
		YamlConfiguration config = new YamlConfiguration();
		config.options().header("WARNING! This file is auto generated. Do NOT under any circumstances change any values here.");
		config.set("structures", worldInfo.generateStructures());
		config.set("seed", worldInfo.seed());
		config.set("type", worldInfo.type().name());
		try
		{
			config.save(new File(getWorldName() + "/level.yml"));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
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
		
		return false;
	}
	
	
	private void beginNetherReset()
	{
		mNetherLockout = true;
		
		Bukkit.broadcastMessage(ChatColor.RED +     "[ATTENTION] " + ChatColor.YELLOW + "The nether is being reset! Expect a severe lag spike for a few seconds.");
		
		World nether = Bukkit.getWorld(getWorldName());
		
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
		
		mPortals.clearPortals();
		TaskChain chain = new TaskChain(new WorldDeleteTask(nether), new WorldCreateTask());
		Bukkit.getScheduler().runTaskLater(this, chain, 20L);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
	private void onPlayerTeleport(EntityTeleportEvent event)
	{
		if(mNetherLockout && event.getTo().getWorld().getName().equals(getWorldName()))
			event.setCancelled(true);
	}
	

	void onNetherLoad()
	{
		if(mNetherLockout)
		{
			mNetherLockout = false;
			Bukkit.broadcastMessage(ChatColor.RED + "[ATTENTION] " + ChatColor.WHITE + "Nether regeneration is complete. Access to the nether is available again.");
		}
	}
	
	
}

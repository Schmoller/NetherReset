package au.com.mineauz.NetherReset;

import java.util.Random;

import net.minecraft.server.v1_6_R2.MinecraftServer;
import net.minecraft.server.v1_6_R2.SecondaryWorldServer;
import net.minecraft.server.v1_6_R2.ServerNBTManager;
import net.minecraft.server.v1_6_R2.WorldServer;
import net.minecraft.server.v1_6_R2.WorldSettings;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldCreateTask implements Task
{
	private WorldServer mTemplate;
	private MinecraftServer mServer;
	
	public WorldCreateTask(WorldServer template, MinecraftServer server)
	{
		mTemplate = template;
		mServer = server;
	}
	
	@Override
	public void run()
	{
		NetherReset.logger.info("Re-creating the nether");
		WorldServer newWorld = createNether(mServer.worlds.get(0), new Random().nextLong());
		mServer.worlds.add(newWorld);
		mServer.worldServer[1] = newWorld;
		
		NetherReset.logger.info("New nether in place.");
		Bukkit.getPluginManager().callEvent(new WorldLoadEvent(newWorld.getWorld()));
	}

	private WorldServer createNether(WorldServer parent, long seed)
	{
		WorldSettings settings = new WorldSettings(seed, mTemplate.worldData.getGameType(), mTemplate.worldData.shouldGenerateMapFeatures(), mTemplate.worldData.isHardcore(), mTemplate.worldData.getType());
		WorldServer world = new SecondaryWorldServer(mServer, new ServerNBTManager(mServer.server.getWorldContainer(), "world_nether", true), "world_nether", -1, settings, parent, mServer.methodProfiler, mServer.getLogger(), World.Environment.getEnvironment(-1), null);
		return world;
	}
	
	@Override
	public boolean wasSuccessful()
	{
		return true;
	}
}

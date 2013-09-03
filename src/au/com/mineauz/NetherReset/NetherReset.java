package au.com.mineauz.NetherReset;

import net.minecraft.server.v1_6_R2.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class NetherReset extends JavaPlugin
{
	@Override
	public void onEnable()
	{
	}
	
	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
	{
		if(command.getName().equals("resetNether"))
		{
			World nether = Bukkit.getWorld("world_nether");

			Bukkit.getPluginManager().callEvent(new WorldUnloadEvent(nether));
			
			CraftWorld world = (CraftWorld)nether;
			final CraftServer server = (CraftServer)getServer();
			final WorldServer worldServer = world.getHandle();
		
			server.getServer().worlds.remove(server.getServer().worlds.indexOf(worldServer));
			server.getServer().worldServer[1] = null;
			
			TaskChain chain = new TaskChain(new WorldDeleteTask(nether, "uid.dat"), new WorldCreateTask(worldServer, server.getServer()));
			Bukkit.getScheduler().runTask(this, chain);
			
			return true;
		}
		
		return false;
	}
	
}

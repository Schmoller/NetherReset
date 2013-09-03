package au.com.mineauz.NetherReset;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Map;

import net.minecraft.server.v1_6_R2.RegionFileCache;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;

public class WorldDeleteTask implements Task
{
	private String[] mDeleteExceptions;
	private File mRootFolder;
	private World mWorld;
	private boolean mSucceeded;
	
	public WorldDeleteTask(World world, String... exceptions)
	{
		mDeleteExceptions = exceptions;
		mRootFolder = new File(world.getName());
		mWorld = world;
	}
	
	@Override
	public void run()
	{
		RegionFileCache.a();
		
		try
		{
			delete(mRootFolder);
			removeCraftWorld();
			
			mSucceeded = true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			mSucceeded = false;
		}
	}
	
	public boolean wasSuccessful()
	{
		return mSucceeded;
	}

	private boolean delete(File root) throws IOException
	{
		for(String exception : mDeleteExceptions)
		{
			if(root.getName().equals(exception))
				return false;
		}
		
		boolean shouldDelete = true;
		
		if(root.isDirectory())
		{
			for(File file : root.listFiles())
			{
				if(!delete(file))
					shouldDelete = false;
			}
		}

		if(!shouldDelete || root.equals(mRootFolder))
			return false;
		
		Files.delete(root.toPath());
		
		return true;
	}
	
	@SuppressWarnings( "unchecked" )
	private void removeCraftWorld() throws Exception
	{
		Map<String, World> worlds;
		Field field = CraftServer.class.getDeclaredField("worlds");
		field.setAccessible(true);
		worlds = (Map<String, World>) field.get(Bukkit.getServer());
		
		worlds.remove(mWorld.getName());
	}
}

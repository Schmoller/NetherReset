package au.com.mineauz.NetherReset;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import net.minecraft.server.v1_6_R2.RegionFileCache;

import org.bukkit.World;

public class WorldDeleteTask implements Task
{
	private String[] mDeleteExceptions;
	private File mRootFolder;
	private boolean mSucceeded;
	private int mAttempts = 0;
	
	public WorldDeleteTask(World world, String... exceptions)
	{
		mDeleteExceptions = exceptions;
		mRootFolder = new File(world.getName());
	}
	
	@Override
	public void run()
	{
		++mAttempts;
		RegionFileCache.a();
		
		try
		{
			NetherReset.logger.info("Deleting world files");
			delete(mRootFolder);
			
			mSucceeded = true;
		}
		catch(Exception e)
		{
			if(canRetry())
				NetherReset.logger.warning("An error occured during nether deletion. Some of the region files may have failed to close. This task will be retried. If you do not see this again, it is not a problem.");
			
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
	
	@Override
	public boolean canRetry()
	{
		return mAttempts < 2;
	}
}

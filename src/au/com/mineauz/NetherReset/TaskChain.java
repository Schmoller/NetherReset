package au.com.mineauz.NetherReset;

import java.util.Arrays;

import org.bukkit.Bukkit;

public class TaskChain implements Runnable
{
	private Task[] mTasks;
	
	public TaskChain(Task... tasks)
	{
		mTasks = tasks;
	}
	@Override
	public void run()
	{
		for(int i = 0; i < mTasks.length; ++i)
		{
			Task task = mTasks[i];
			
			task.run();
			
			if(!task.wasSuccessful())
			{
				
				if(task.canRetry())
				{
					TaskChain chain = new TaskChain(Arrays.copyOfRange(mTasks, i, mTasks.length));
					Bukkit.getScheduler().runTaskLater(NetherReset.instance, chain, 20L);
				}
				return;
			}
		}
	}

}

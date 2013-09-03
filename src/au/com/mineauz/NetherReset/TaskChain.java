package au.com.mineauz.NetherReset;

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
				return;
		}
	}

}

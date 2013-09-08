package au.com.mineauz.NetherReset;

public class WorldCreateTask implements Task
{
	public WorldCreateTask()
	{
	}
	
	@Override
	public void run()
	{
		NetherReset.logger.info("Re-creating the nether");
		NetherReset.instance.createNewNether();
		
		NetherReset.logger.info("New nether in place.");
		NetherReset.instance.onNetherLoad();
	}

	@Override
	public boolean wasSuccessful()
	{
		return true;
	}
	
	@Override
	public boolean canRetry()
	{
		return false;
	}
}

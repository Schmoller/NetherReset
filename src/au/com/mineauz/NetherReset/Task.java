package au.com.mineauz.NetherReset;

public interface Task extends Runnable
{
	public boolean wasSuccessful();
	
	public boolean canRetry();
}

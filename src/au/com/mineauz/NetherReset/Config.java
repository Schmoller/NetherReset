package au.com.mineauz.NetherReset;

import java.io.File;

public class Config extends AutoConfig
{
	public Config( File file )
	{
		super(file);

		
	}
	
	@ConfigField(comment="The world to use. Leave blank for auto detect", category="general")
	public String world = "";
}

package au.com.mineauz.NetherReset;

import java.lang.reflect.Field;

public class ReflectionHelper
{
	public static <T> T getFieldValue(Object obj, String field)
	{
		return getFieldValue(obj.getClass(), field, obj);
	}
	@SuppressWarnings( "unchecked" )
	public static <T> T getFieldValue(Class<?> clazz, String field, Object instance)
	{
		try
		{
			System.out.println(clazz.getName());
			Field f = clazz.getDeclaredField(field);
			f.setAccessible(true);
			
			return (T)f.get(instance);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}

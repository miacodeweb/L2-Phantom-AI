package custom.PhantomManager;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.World;

public class PhantomGeo
{
	public static Location getNpcLikeSpawn(Location base)
	{
		int x = clamp(base.getX(), World.WORLD_X_MIN + 5000, World.WORLD_X_MAX - 5000);
		int y = clamp(base.getY(), World.WORLD_Y_MIN + 5000, World.WORLD_Y_MAX - 5000);
		int z = base.getZ();
		if (GeoEngine.getInstance().hasGeo(x, y))
		{
			int geoZ = GeoEngine.getInstance().getHeight(x, y, z);
			if (Math.abs(z - geoZ) < 300)
			{
				z = geoZ;
			}
		}
		return new Location(x, y, z);
	}
	
	public static Location getSafeSpawn(Location base, int radius)
	{
		Location fallback = null;
		for (int i = 0; i < 16; i++)
		{
			int x = base.getX();
			int y = base.getY();
			if ((radius > 0) && (i > 0))
			{
				x += Rnd.get(-radius, radius);
				y += Rnd.get(-radius, radius);
			}
			
			x = clamp(x, World.WORLD_X_MIN + 5000, World.WORLD_X_MAX - 5000);
			y = clamp(y, World.WORLD_Y_MIN + 5000, World.WORLD_Y_MAX - 5000);
			int z = GeoEngine.getInstance().getSpawnHeight(x, y, base.getZ());
			Location candidate = new Location(x, y, z);
			
			if (World.getInstance().getRegion(candidate.getX(), candidate.getY(), candidate.getZ()) == null)
			{
				continue;
			}
			
			if (fallback == null)
			{
				fallback = candidate;
			}
			
			if (GeoEngine.getInstance().hasGeo(candidate.getX(), candidate.getY()))
			{
				return candidate;
			}
		}
		
		if (fallback != null)
		{
			return fallback;
		}
		
		int z = GeoEngine.getInstance().getSpawnHeight(base.getX(), base.getY(), base.getZ());
		return new Location(base.getX(), base.getY(), z);
	}
	
	private static int clamp(int value, int min, int max)
	{
		return Math.max(min, Math.min(max, value));
	}
}

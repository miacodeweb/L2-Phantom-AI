package custom.PhantomManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.gameserver.model.Location;

public class PhantomState
{
	public static final Map<Integer, List<Location>> NPC_ANCHORS = new ConcurrentHashMap<>();
	public static final Map<Integer, Boolean> MP_RECOVERY_STATE = new ConcurrentHashMap<>();
	public static final Map<Integer, Integer> GEAR_GRADE = new ConcurrentHashMap<>();
	public static final Map<Integer, Integer> STUCK_COUNTERS = new ConcurrentHashMap<>();
	public static final Map<Integer, Integer> HUNT_LEVEL_BAND = new ConcurrentHashMap<>();
	public static final Map<Integer, Long> NEXT_HUNT_TELEPORT = new ConcurrentHashMap<>();
	public static final Set<Integer> AGGRESSIVE_PHANTOMS = ConcurrentHashMap.newKeySet();
	public static final Set<Integer> PK_PHANTOMS = ConcurrentHashMap.newKeySet();
	public static final Set<Integer> REVIVING_PHANTOMS = ConcurrentHashMap.newKeySet();
	
	public static void register(int objectId, boolean aggressive, boolean pkMode)
	{
		MP_RECOVERY_STATE.put(objectId, false);
		GEAR_GRADE.put(objectId, -1);
		STUCK_COUNTERS.put(objectId, 0);
		HUNT_LEVEL_BAND.put(objectId, -1);
		NEXT_HUNT_TELEPORT.put(objectId, 0L);
		if (aggressive)
		{
			AGGRESSIVE_PHANTOMS.add(objectId);
		}
		if (pkMode)
		{
			PK_PHANTOMS.add(objectId);
		}
	}
	
	public static void unregister(int objectId)
	{
		MP_RECOVERY_STATE.remove(objectId);
		GEAR_GRADE.remove(objectId);
		STUCK_COUNTERS.remove(objectId);
		HUNT_LEVEL_BAND.remove(objectId);
		NEXT_HUNT_TELEPORT.remove(objectId);
		AGGRESSIVE_PHANTOMS.remove(objectId);
		PK_PHANTOMS.remove(objectId);
		REVIVING_PHANTOMS.remove(objectId);
	}
	
	public static void clear()
	{
		MP_RECOVERY_STATE.clear();
		GEAR_GRADE.clear();
		STUCK_COUNTERS.clear();
		HUNT_LEVEL_BAND.clear();
		NEXT_HUNT_TELEPORT.clear();
		NPC_ANCHORS.clear();
		AGGRESSIVE_PHANTOMS.clear();
		PK_PHANTOMS.clear();
		REVIVING_PHANTOMS.clear();
	}
	
	public static List<Location> getAnchors(int level)
	{
		return NPC_ANCHORS.computeIfAbsent(level, k -> new ArrayList<>());
	}
	
	public static boolean isAggressive(int objectId)
	{
		return AGGRESSIVE_PHANTOMS.contains(objectId);
	}
	
	public static boolean isPk(int objectId)
	{
		return PK_PHANTOMS.contains(objectId);
	}
}

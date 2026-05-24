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
	public static final Map<Integer, Integer> GEAR_PACK = new ConcurrentHashMap<>();
	public static final Map<Integer, Integer> STUCK_COUNTERS = new ConcurrentHashMap<>();
	public static final Map<Integer, Integer> EMPTY_TARGET_COUNTERS = new ConcurrentHashMap<>();
	public static final Map<Integer, Integer> HUNT_LEVEL_BAND = new ConcurrentHashMap<>();
	public static final Map<Integer, Integer> GOAL_LEVEL = new ConcurrentHashMap<>();
	public static final Map<Integer, Long> NEXT_HUNT_TELEPORT = new ConcurrentHashMap<>();
	public static final Map<Integer, Long> LAST_AI_TRACE = new ConcurrentHashMap<>();
	public static final Map<Integer, Long> CITY_REST_UNTIL = new ConcurrentHashMap<>();
	public static final Map<Integer, Long> LAST_SELF_BUFF = new ConcurrentHashMap<>();
	public static final Set<Integer> AGGRESSIVE_PHANTOMS = ConcurrentHashMap.newKeySet();
	public static final Set<Integer> PK_PHANTOMS = ConcurrentHashMap.newKeySet();
	public static final Set<Integer> REVIVING_PHANTOMS = ConcurrentHashMap.newKeySet();
	
	public static void register(int objectId, boolean aggressive, boolean pkMode)
	{
		MP_RECOVERY_STATE.put(objectId, false);
		GEAR_GRADE.put(objectId, -1);
		GEAR_PACK.put(objectId, -1);
		STUCK_COUNTERS.put(objectId, 0);
		EMPTY_TARGET_COUNTERS.put(objectId, 0);
		HUNT_LEVEL_BAND.put(objectId, -1);
		GOAL_LEVEL.put(objectId, 0);
		NEXT_HUNT_TELEPORT.put(objectId, 0L);
		LAST_AI_TRACE.put(objectId, 0L);
		CITY_REST_UNTIL.put(objectId, 0L);
		LAST_SELF_BUFF.put(objectId, 0L);
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
		GEAR_PACK.remove(objectId);
		STUCK_COUNTERS.remove(objectId);
		EMPTY_TARGET_COUNTERS.remove(objectId);
		HUNT_LEVEL_BAND.remove(objectId);
		GOAL_LEVEL.remove(objectId);
		NEXT_HUNT_TELEPORT.remove(objectId);
		LAST_AI_TRACE.remove(objectId);
		CITY_REST_UNTIL.remove(objectId);
		LAST_SELF_BUFF.remove(objectId);
		AGGRESSIVE_PHANTOMS.remove(objectId);
		PK_PHANTOMS.remove(objectId);
		REVIVING_PHANTOMS.remove(objectId);
	}
	
	public static void clear()
	{
		MP_RECOVERY_STATE.clear();
		GEAR_GRADE.clear();
		GEAR_PACK.clear();
		STUCK_COUNTERS.clear();
		EMPTY_TARGET_COUNTERS.clear();
		HUNT_LEVEL_BAND.clear();
		GOAL_LEVEL.clear();
		NEXT_HUNT_TELEPORT.clear();
		LAST_AI_TRACE.clear();
		CITY_REST_UNTIL.clear();
		LAST_SELF_BUFF.clear();
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

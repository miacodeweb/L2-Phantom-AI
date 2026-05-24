package custom.PhantomManager;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.Intention;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.item.enums.ShotType;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.util.LocationUtil;

public class PhantomAI
{
	private static final int MP_REST_PERCENT = 10;
	private static final int MP_READY_PERCENT = 100;
	private static final int EMPTY_TARGET_RELOCATE_LIMIT = 3;
	private static final int GOAL_LEVEL_STEP_MIN = 4;
	private static final int GOAL_LEVEL_STEP_MAX = 6;
	private static final int CITY_REST_MIN_TIME = 45000;
	private static final int CITY_REST_MAX_TIME = 90000;
	
	public static void thinkAndFarm(Player bot)
	{
		if (handleLifeGoal(bot))
		{
			return;
		}
		
		if (handleMageMpRecovery(bot))
		{
			trace(bot, "modo reposo/escape por MP");
			return;
		}
		
		int stuckCount = PhantomState.STUCK_COUNTERS.getOrDefault(bot.getObjectId(), 0);
		
		if (stuckCount >= 2)
		{
			trace(bot, "atascado, relocalizando");
			relocateStuckBot(bot);
			return;
		}
		
		if (bot.isCastingNow() || bot.isAttackingNow() || bot.isMoving())
		{
			trace(bot, "ocupado: moving=" + bot.isMoving() + " casting=" + bot.isCastingNow() + " attacking=" + bot.isAttackingNow());
			return;
		}
		
		if (PhantomHuntingSpots.relocateForLevelIfNeeded(bot))
		{
			trace(bot, "teleport por rango de level");
			return;
		}
		
		tryCasualChat(bot);
		
		Player aggressor = findPvPAttacker(bot);
		if (aggressor != null)
		{
			trace(bot, "defendiendo contra " + aggressor.getName());
			bot.setTarget(aggressor);
			executeAttackPlan(bot, aggressor);
			return;
		}
		
		Player victim = findAggressiveTarget(bot);
		if (victim != null)
		{
			trace(bot, "objetivo PvP/PK " + victim.getName());
			makePkIfNeeded(bot, victim);
			bot.setTarget(victim);
			executeAttackPlan(bot, victim);
			return;
		}
		
		Monster target = findFarmTarget(bot);
		if (target != null)
		{
			PhantomState.EMPTY_TARGET_COUNTERS.put(bot.getObjectId(), 0);
			PhantomManager.logToFile(bot.getName(), "Atacando a " + target.getName());
			bot.setTarget(target);
			executeAttackPlan(bot, target);
			return;
		}
		
		handleNoFarmTarget(bot);
	}
	
	private static boolean handleLifeGoal(Player bot)
	{
		int objectId = bot.getObjectId();
		long now = System.currentTimeMillis();
		long restUntil = PhantomState.CITY_REST_UNTIL.getOrDefault(objectId, 0L);
		
		if (restUntil > now)
		{
			recoverInTown(bot);
			trace(bot, "descansando en ciudad");
			return true;
		}
		
		if (restUntil != 0L)
		{
			PhantomState.CITY_REST_UNTIL.put(objectId, 0L);
			assignNewGoal(bot);
			if (bot.isSitting())
			{
				bot.standUp();
			}
			PhantomHuntingSpots.travelToLevelSpot(bot, "Descanso terminado. Vuelve a farmear");
			trace(bot, "sale de ciudad hacia nuevo spot");
			return true;
		}
		
		int goalLevel = PhantomState.GOAL_LEVEL.getOrDefault(objectId, 0);
		if (goalLevel <= bot.getLevel())
		{
			if (goalLevel <= 0)
			{
				assignNewGoal(bot);
				return false;
			}
			
			travelToNearestTown(bot, "Meta cumplida L" + bot.getLevel() + "/" + goalLevel);
			return true;
		}
		return false;
	}
	
	private static void assignNewGoal(Player bot)
	{
		int goal = Math.min(99, bot.getLevel() + Rnd.get(GOAL_LEVEL_STEP_MIN, GOAL_LEVEL_STEP_MAX));
		PhantomState.GOAL_LEVEL.put(bot.getObjectId(), goal);
		PhantomManager.logToFile(bot.getName(), "Nueva meta: subir hasta nivel " + goal);
	}
	
	private static void travelToNearestTown(Player bot, String reason)
	{
		PhantomConfig.FarmZone nearest = null;
		long bestDistance = Long.MAX_VALUE;
		for (PhantomConfig.FarmZone zone : PhantomConfig.ROUTES)
		{
			long dx = bot.getX() - zone.townX;
			long dy = bot.getY() - zone.townY;
			long distance = (dx * dx) + (dy * dy);
			if (distance < bestDistance)
			{
				bestDistance = distance;
				nearest = zone;
			}
		}
		
		if (nearest == null)
		{
			return;
		}
		
		Location safeTown = PhantomGeo.getNpcLikeSpawn(new Location(nearest.townX, nearest.townY, nearest.townZ));
		PhantomEngine.movePhantomTo(bot, safeTown, reason + ". Viaja a ciudad cercana");
		PhantomState.HUNT_LEVEL_BAND.put(bot.getObjectId(), -1);
		PhantomState.CITY_REST_UNTIL.put(bot.getObjectId(), System.currentTimeMillis() + Rnd.get(CITY_REST_MIN_TIME, CITY_REST_MAX_TIME));
		PhantomManager.logToFile(bot.getName(), reason + ". Descansa en ciudad hasta recuperar recursos.");
	}
	
	private static void recoverInTown(Player bot)
	{
		if (bot.isDead())
		{
			return;
		}
		if (bot.isMoving())
		{
			return;
		}
		if (!bot.isSitting())
		{
			bot.getAI().setIntention(Intention.REST);
			bot.sitDown();
		}
		
		double mpGain = Math.max(1, bot.getMaxMp() * 0.08);
		double hpGain = Math.max(1, bot.getMaxHp() * 0.04);
		bot.setCurrentMp(Math.min(bot.getMaxMp(), bot.getCurrentMp() + mpGain));
		bot.setCurrentHp(Math.min(bot.getMaxHp(), bot.getCurrentHp() + hpGain));
		bot.broadcastUserInfo();
	}
	
	private static boolean handleMageMpRecovery(Player bot)
	{
		if (!bot.isMageClass())
		{
			return false;
		}
		
		boolean recovering = PhantomState.MP_RECOVERY_STATE.getOrDefault(bot.getObjectId(), false);
		if (!recovering && (bot.getCurrentMpPercent() <= MP_REST_PERCENT))
		{
			recovering = true;
			PhantomState.MP_RECOVERY_STATE.put(bot.getObjectId(), true);
			PhantomManager.logToFile(bot.getName(), "Sin MP. Entrando en modo reposo.");
		}
		
		if (!recovering)
		{
			return false;
		}
		
		if (bot.getCurrentMpPercent() >= MP_READY_PERCENT)
		{
			PhantomState.MP_RECOVERY_STATE.put(bot.getObjectId(), false);
			if (bot.isSitting())
			{
				bot.standUp();
			}
			PhantomManager.logToFile(bot.getName(), "MP completo. Vuelve al combate.");
			return false;
		}
		
		Player danger = findPvPAttacker(bot);
		if (danger != null)
		{
			if (bot.isSitting())
			{
				bot.standUp();
			}
			runAwayFrom(bot, danger);
			return true;
		}
		
		if (!bot.isSitting() && !bot.isMoving())
		{
			bot.getAI().setIntention(Intention.REST);
			bot.sitDown();
		}
		return true;
	}
	
	private static void relocateStuckBot(Player bot)
	{
		Location anchor = getSafeAnchor(bot.getLevel());
		if (anchor != null)
		{
			Location safe = PhantomGeo.getNpcLikeSpawn(anchor);
			PhantomManager.logToFile(bot.getName(), "Viajando a anclaje de NPC: " + safe.getX() + ", " + safe.getY() + ", " + safe.getZ());
			PhantomEngine.movePhantomTo(bot, safe, "Viajando a anclaje de NPC");
		}
		else
		{
			PhantomConfig.FarmZone zone = PhantomConfig.getIdealZone(bot.getLevel());
			int safeZ = GeoEngine.getInstance().getHeight(zone.spotX, zone.spotY, zone.spotZ);
			PhantomManager.logToFile(bot.getName(), "Calculando Z con GeoEngine: " + safeZ);
			PhantomEngine.movePhantomTo(bot, new Location(zone.spotX + Rnd.get(-100, 100), zone.spotY + Rnd.get(-100, 100), safeZ), "Viajando a zona fallback");
		}
		bot.broadcastUserInfo();
		PhantomState.STUCK_COUNTERS.put(bot.getObjectId(), 0);
	}
	
	private static Location getSafeAnchor(int level)
	{
		for (int i = level - 3; i <= (level + 3); i++)
		{
			List<Location> anchors = PhantomState.NPC_ANCHORS.get(i);
			if ((anchors != null) && !anchors.isEmpty())
			{
				return anchors.get(Rnd.get(anchors.size()));
			}
		}
		return null;
	}
	
	private static void tryCasualChat(Player bot)
	{
		if (Rnd.get(100) < 1)
		{
			Player nearby = World.getInstance().getVisibleObjectsInRange(bot, Player.class, 1000).stream().filter(p -> !p.isDead() && !p.isGM() && !PhantomEngine.activePhantoms.contains(p)).findFirst().orElse(null);
			if (nearby != null)
			{
				PhantomChat.requestGemini(bot, nearby, "Dile algo casual y amigable a un jugador llamado " + nearby.getName() + " que paso por tu lado.", false);
			}
		}
	}
	
	private static Player findPvPAttacker(Player bot)
	{
		for (Player p : World.getInstance().getVisibleObjectsInRange(bot, Player.class, 1500))
		{
			if ((p == bot) || p.isDead() || p.isGM() || (p.getTarget() != bot))
			{
				continue;
			}
			
			if (PhantomEngine.activePhantoms.contains(p))
			{
				return p;
			}
			else if (p.getPvpFlag() > 0)
			{
				return p;
			}
		}
		return null;
	}
	
	private static Player findAggressiveTarget(Player bot)
	{
		if (!PhantomState.isAggressive(bot.getObjectId()) || (Rnd.get(100) >= 6))
		{
			return null;
		}
		
		boolean pkMode = PhantomState.isPk(bot.getObjectId());
		Player closest = null;
		double closestDistance = Double.MAX_VALUE;
		for (Player p : World.getInstance().getVisibleObjectsInRange(bot, Player.class, 1200))
		{
			if (p.isDead() || p.isGM() || (p == bot))
			{
				continue;
			}
			if (PhantomEngine.activePhantoms.contains(p))
			{
				if (pkMode || (Rnd.get(100) >= 35))
				{
					continue;
				}
			}
			else if (!pkMode && (p.getPvpFlag() <= 0))
			{
				continue;
			}
			
			double distance = LocationUtil.calculateDistance(bot, p, true, false);
			if (distance < closestDistance)
			{
				closest = p;
				closestDistance = distance;
			}
		}
		return closest;
	}
	
	private static void makePkIfNeeded(Player bot, Player victim)
	{
		if (!PhantomState.isPk(bot.getObjectId()) || (victim.getPvpFlag() > 0))
		{
			if (PhantomEngine.activePhantoms.contains(victim))
			{
				bot.startPvPFlag();
				victim.startPvPFlag();
			}
			return;
		}
		bot.setPkKills(Math.max(1, bot.getPkKills()));
		bot.setReputation(-Rnd.get(360, 3600));
		bot.updatePvpTitleAndColor(true);
		bot.broadcastReputation();
		bot.broadcastUserInfo();
		PhantomManager.logToFile(bot.getName(), "Modo PK activado contra " + victim.getName());
	}
	
	private static Monster findFarmTarget(Player bot)
	{
		List<Monster> idealMobs = new ArrayList<>();
		List<Monster> backupMobs = new ArrayList<>();
		boolean foundMobs = false;
		
		for (Monster m : World.getInstance().getVisibleObjectsInRange(bot, Monster.class, 2500))
		{
			if (!m.isDead() && m.isAttackable() && !isForbiddenFarmTarget(m))
			{
				foundMobs = true;
				List<Location> anchors = PhantomState.getAnchors(m.getLevel());
				anchors.add(new Location(m.getX(), m.getY(), m.getZ()));
				if (anchors.size() > 50)
				{
					anchors.remove(0);
				}
				
				if (!isTargetedByOtherPhantom(bot, m))
				{
					int lvlDiff = m.getLevel() - bot.getLevel();
					if ((lvlDiff >= -8) && (lvlDiff <= 2))
					{
						idealMobs.add(m);
					}
					else
					{
						backupMobs.add(m);
					}
				}
			}
		}
		
		if (!foundMobs)
		{
			int stuckCount = PhantomState.STUCK_COUNTERS.getOrDefault(bot.getObjectId(), 0) + 1;
			PhantomState.STUCK_COUNTERS.put(bot.getObjectId(), stuckCount);
			PhantomManager.logToFile(bot.getName(), "No encuentra mobs. Contador atascado: " + stuckCount);
			return null;
		}
		
		PhantomState.STUCK_COUNTERS.put(bot.getObjectId(), 0);
		if (idealMobs.isEmpty() && backupMobs.isEmpty())
		{
			PhantomManager.logToFile(bot.getName(), "Hay mobs cerca, pero todos estan ocupados por otros phantoms.");
			return null;
		}
		return !idealMobs.isEmpty() ? getNearestMob(bot, idealMobs) : (!backupMobs.isEmpty() ? getNearestMob(bot, backupMobs) : null);
	}
	
	private static boolean isForbiddenFarmTarget(Monster monster)
	{
		return PhantomHuntingSpots.isForbiddenTargetName(monster.getName());
	}
	
	private static Monster getNearestMob(Player bot, List<Monster> mobs)
	{
		Monster closest = null;
		double closestDistance = Double.MAX_VALUE;
		for (Monster mob : mobs)
		{
			double distance = LocationUtil.calculateDistance(bot, mob, true, false);
			if (distance < closestDistance)
			{
				closest = mob;
				closestDistance = distance;
			}
		}
		return closest;
	}
	
	private static void handleNoFarmTarget(Player bot)
	{
		int objectId = bot.getObjectId();
		int emptyCount = PhantomState.EMPTY_TARGET_COUNTERS.getOrDefault(objectId, 0) + 1;
		PhantomState.EMPTY_TARGET_COUNTERS.put(objectId, emptyCount);
		
		if (emptyCount >= EMPTY_TARGET_RELOCATE_LIMIT)
		{
			PhantomState.EMPTY_TARGET_COUNTERS.put(objectId, 0);
			PhantomState.HUNT_LEVEL_BAND.put(objectId, -1);
			PhantomState.NEXT_HUNT_TELEPORT.put(objectId, 0L);
			if (PhantomHuntingSpots.travelToLevelSpot(bot, "Sin mobs utiles. Busca otro spot L" + bot.getLevel()))
			{
				trace(bot, "reubicado por falta de mobs utiles");
				return;
			}
		}
		
		trace(bot, "sin objetivo util, espera");
	}
	
	private static boolean isTargetedByOtherPhantom(Player bot, Monster monster)
	{
		if (LocationUtil.calculateDistance(bot, monster, true, false) <= 450)
		{
			return false;
		}
		for (Player p : PhantomEngine.activePhantoms)
		{
			if ((p != bot) && (p.getTarget() == monster))
			{
				return true;
			}
		}
		return false;
	}
	
	private static void runAwayFrom(Player bot, Player danger)
	{
		int dx = bot.getX() - danger.getX();
		int dy = bot.getY() - danger.getY();
		if ((dx == 0) && (dy == 0))
		{
			dx = Rnd.get(-1, 1);
			dy = Rnd.get(-1, 1);
		}
		
		int newX = bot.getX() + (dx > 0 ? 1400 : -1400);
		int newY = bot.getY() + (dy > 0 ? 1400 : -1400);
		Location destination = GeoEngine.getInstance().getValidLocation(bot.getX(), bot.getY(), bot.getZ(), newX, newY, GeoEngine.getInstance().getHeight(newX, newY, bot.getZ()), bot.getInstanceWorld());
		bot.getAI().setIntention(Intention.MOVE_TO, destination);
		PhantomManager.logToFile(bot.getName(), "Sin MP en PvP. Escapando de " + danger.getName());
	}
	
	private static void executeAttackPlan(Player bot, Creature target)
	{
		if (!bot.isChargedShot(ShotType.SOULSHOTS))
		{
			bot.chargeShot(ShotType.SOULSHOTS);
		}
		if (!bot.isChargedShot(ShotType.BLESSED_SPIRITSHOTS))
		{
			bot.chargeShot(ShotType.BLESSED_SPIRITSHOTS);
		}
		
		List<Skill> offensiveSkills = new ArrayList<>();
		for (Skill sk : bot.getAllSkills())
		{
			if ((sk != null) && sk.hasNegativeEffect() && !sk.isPassive())
			{
				offensiveSkills.add(sk);
			}
		}
		
		if (!offensiveSkills.isEmpty() && (bot.getCurrentMpPercent() > MP_REST_PERCENT) && (Rnd.get(100) < 30))
		{
			PhantomManager.logToFile(bot.getName(), "Casteando contra " + target.getName());
			bot.getAI().setIntention(Intention.CAST, offensiveSkills.get(Rnd.get(offensiveSkills.size())), target);
		}
		else
		{
			PhantomManager.logToFile(bot.getName(), "Ataque fisico contra " + target.getName());
			int range = Math.max(80, bot.getPhysicalAttackRange());
			if (LocationUtil.calculateDistance(bot, target, true, false) > (range + 40))
			{
				Location destination = GeoEngine.getInstance().getValidLocation(bot.getX(), bot.getY(), bot.getZ(), target.getX(), target.getY(), target.getZ(), bot.getInstanceWorld());
				bot.getAI().setIntention(Intention.MOVE_TO, destination);
				PhantomManager.logToFile(bot.getName(), "Se acerca a " + target.getName() + ": " + destination.getX() + ", " + destination.getY() + ", " + destination.getZ());
				return;
			}
			bot.doAutoAttack(target);
		}
	}
	
	private static void trace(Player bot, String message)
	{
		long now = System.currentTimeMillis();
		long last = PhantomState.LAST_AI_TRACE.getOrDefault(bot.getObjectId(), 0L);
		if ((now - last) >= 15000L)
		{
			PhantomState.LAST_AI_TRACE.put(bot.getObjectId(), now);
			PhantomManager.logToFile(bot.getName(), "AI " + message);
		}
	}
}

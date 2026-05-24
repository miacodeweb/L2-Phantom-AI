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
	
	public static void thinkAndFarm(Player bot)
	{
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
			PhantomManager.logToFile(bot.getName(), "Atacando a " + target.getName());
			bot.setTarget(target);
			executeAttackPlan(bot, target);
			return;
		}
		
		trace(bot, "sin objetivo, caminando");
		wander(bot);
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
				if (p.isAttackingNow() || p.isCastingNow() || PhantomState.isPk(p.getObjectId()))
				{
					return p;
				}
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
		return World.getInstance().getVisibleObjectsInRange(bot, Player.class, 1200).stream().filter(p -> !p.isDead() && !p.isGM() && (p != bot) && !PhantomEngine.activePhantoms.contains(p)).filter(p -> pkMode || (p.getPvpFlag() > 0)).findFirst().orElse(null);
	}
	
	private static void makePkIfNeeded(Player bot, Player victim)
	{
		if (!PhantomState.isPk(bot.getObjectId()) || (victim.getPvpFlag() > 0))
		{
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
			if (!m.isDead() && m.isAttackable())
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
		return !idealMobs.isEmpty() ? idealMobs.get(Rnd.get(idealMobs.size())) : (!backupMobs.isEmpty() ? backupMobs.get(Rnd.get(backupMobs.size())) : null);
	}
	
	private static boolean isTargetedByOtherPhantom(Player bot, Monster monster)
	{
		for (Player p : PhantomEngine.activePhantoms)
		{
			if ((p != bot) && (p.getTarget() == monster))
			{
				return true;
			}
		}
		return false;
	}
	
	private static void wander(Player bot)
	{
		int newX = bot.getX();
		int newY = bot.getY();
		Player closePhantom = World.getInstance().getVisibleObjectsInRange(bot, Player.class, 400).stream().filter(p -> PhantomEngine.activePhantoms.contains(p) && (p != bot)).findFirst().orElse(null);
		
		if (closePhantom != null)
		{
			newX += ((bot.getX() - closePhantom.getX()) > 0 ? 1500 : -1500);
			newY += ((bot.getY() - closePhantom.getY()) > 0 ? 1500 : -1500);
		}
		else
		{
			newX += Rnd.get(-1500, 1500);
			newY += Rnd.get(-1500, 1500);
		}
		
		Location destination = GeoEngine.getInstance().getValidLocation(bot.getX(), bot.getY(), bot.getZ(), newX, newY, GeoEngine.getInstance().getHeight(newX, newY, bot.getZ()), bot.getInstanceWorld());
		PhantomManager.logToFile(bot.getName(), "Caminando a " + destination.getX() + ", " + destination.getY() + ", " + destination.getZ());
		bot.getAI().setIntention(Intention.MOVE_TO, destination);
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

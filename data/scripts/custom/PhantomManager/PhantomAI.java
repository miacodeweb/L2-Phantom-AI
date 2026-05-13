package custom.PhantomManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.Intention;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.enums.player.PlayerClass;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.model.item.enums.ShotType;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;

public class PhantomAI
{
	public static final Map<Integer, List<Location>> _npcAnchors = new ConcurrentHashMap<>();
	public static final Map<Integer, Boolean> _mpRecoveryState = new ConcurrentHashMap<>();
	public static final Map<Integer, Integer> _phantomGearGrade = new ConcurrentHashMap<>();
	public static final Map<Integer, Integer> _stuckCounters = new ConcurrentHashMap<>();
	
	public static void checkProgression(Player bot)
	{
		int lvl = bot.getLevel();
		PlayerClass currentClass = bot.getPlayerClass();
		int classLevel = currentClass.level();
		boolean needsClassChange = ((lvl >= 20) && (classLevel == 0)) || ((lvl >= 40) && (classLevel == 1)) || ((lvl >= 76) && (classLevel == 2)) || ((lvl >= 85) && (classLevel == 3));
		
		if (needsClassChange)
		{
			List<PlayerClass> nextClasses = new ArrayList<>();
			for (PlayerClass cid : PlayerClass.values())
			{
				if (cid.getParent() == currentClass)
				{
					nextClasses.add(cid);
				}
			}
			if (!nextClasses.isEmpty())
			{
				bot.setPlayerClass(nextClasses.get(Rnd.get(nextClasses.size())).getId());
				bot.setBaseClass(bot.getPlayerClass().getId());
				bot.broadcastUserInfo();
			}
		}
		
		int targetGrade = (lvl >= 76) ? 5 : (lvl >= 61) ? 4 : (lvl >= 52) ? 3 : (lvl >= 40) ? 2 : (lvl >= 20) ? 1 : 0;
		if (targetGrade > _phantomGearGrade.getOrDefault(bot.getObjectId(), -1))
		{
			_phantomGearGrade.put(bot.getObjectId(), targetGrade);
			int[] gearSet = bot.isMageClass() ? PhantomConfig.MAGE_GEAR[targetGrade] : PhantomConfig.FIGHTER_GEAR[targetGrade];
			for (int itemId : gearSet)
			{
				Item item = bot.getInventory().getItemByItemId(itemId);
				if (item == null)
				{
					item = bot.addItem(ItemProcessType.REWARD, itemId, 1L, bot, false);
				}
				if ((item != null) && !item.isEquipped())
				{
					bot.getInventory().equipItem(item);
				}
			}
			bot.broadcastUserInfo();
		}
		
		int shotId = bot.isMageClass() ? PhantomConfig.MAGE_SHOTS[targetGrade] : PhantomConfig.FIGHTER_SHOTS[targetGrade];
		if (bot.getInventory().getInventoryItemCount(shotId, -1) < 100)
		{
			bot.addItem(ItemProcessType.REWARD, shotId, 1000, bot, false);
			bot.addAutoSoulShot(shotId);
			bot.broadcastUserInfo();
		}
	}
	
	public static void cleanInventory(Player bot)
	{
		if (bot.getInventory().getSize() > 10)
		{
			for (Item item : bot.getInventory().getItems())
			{
				if (!item.isEquipped() && (item.getId() != 57) && !isShot(item.getId()))
				{
					bot.destroyItem(ItemProcessType.DESTROY, item, bot, false);
				}
			}
			bot.broadcastUserInfo();
		}
	}
	
	private static boolean isShot(int itemId)
	{
		for (int id : PhantomConfig.FIGHTER_SHOTS)
		{
			if (id == itemId)
			{
				return true;
			}
		}
		for (int id : PhantomConfig.MAGE_SHOTS)
		{
			if (id == itemId)
			{
				return true;
			}
		}
		return false;
	}
	
	public static void applyBasicBuffs(Player bot)
	{
		if (bot.getEffectList().getBuffCount() < 3)
		{
			for (int id : new int[]
			{
				1086,
				1204,
				1068
			})
			{
				Skill buff = SkillData.getInstance().getSkill(id, 2);
				if (buff != null)
				{
					buff.applyEffects(bot, bot);
				}
			}
		}
	}
	
	private static Location getSafeAnchor(int level)
	{
		for (int i = level - 3; i <= (level + 3); i++)
		{
			if (_npcAnchors.containsKey(i) && !_npcAnchors.get(i).isEmpty())
			{
				return _npcAnchors.get(i).get(Rnd.get(_npcAnchors.get(i).size()));
			}
		}
		return null;
	}
	
	public static void thinkAndFarm(Player bot)
	{
		int stuckCount = _stuckCounters.getOrDefault(bot.getObjectId(), 0);
		
		if (stuckCount >= 2)
		{
			Location anchor = getSafeAnchor(bot.getLevel());
			if (anchor != null)
			{
				PhantomManager.logToFile(bot.getName(), "Viajando a anclaje de NPC: " + anchor.getX() + ", " + anchor.getY() + ", " + anchor.getZ());
				bot.teleToLocation(anchor.getX() + Rnd.get(-50, 50), anchor.getY() + Rnd.get(-50, 50), anchor.getZ());
			}
			else
			{
				PhantomConfig.FarmZone zone = PhantomConfig.getIdealZone(bot.getLevel());
				int safeZ = GeoEngine.getInstance().getHeight(zone.spotX, zone.spotY, zone.spotZ);
				PhantomManager.logToFile(bot.getName(), "Calculando Z con GeoEngine: " + safeZ);
				bot.teleToLocation(zone.spotX + Rnd.get(-100, 100), zone.spotY + Rnd.get(-100, 100), safeZ);
			}
			bot.broadcastUserInfo();
			_stuckCounters.put(bot.getObjectId(), 0);
			return;
		}
		
		if (bot.isCastingNow() || bot.isAttackingNow() || bot.isMoving())
		{
			return;
		}
		
		if (Rnd.get(100) < 1)
		{
			Player nearby = World.getInstance().getVisibleObjectsInRange(bot, Player.class, 1000).stream().filter(p -> !p.isDead() && !p.isGM() && !PhantomManager.activePhantoms.contains(p)).findFirst().orElse(null);
			if (nearby != null)
			{
				PhantomChat.requestGemini(bot, nearby, "Dile algo casual y amigable a un jugador llamado " + nearby.getName() + " que pasó por tu lado.", false);
			}
		}
		
		Player aggressor = World.getInstance().getVisibleObjectsInRange(bot, Player.class, 1500).stream().filter(p -> !p.isDead() && (p.getTarget() == bot) && (p.getPvpFlag() > 0) && !p.isGM()).findFirst().orElse(null);
		if (aggressor != null)
		{
			bot.setTarget(aggressor);
			executeAttackPlan(bot, aggressor);
			return;
		}
		
		List<Monster> idealMobs = new ArrayList<>();
		List<Monster> backupMobs = new ArrayList<>();
		boolean foundMobs = false;
		
		for (Monster m : World.getInstance().getVisibleObjectsInRange(bot, Monster.class, 2500))
		{
			if (!m.isDead() && m.isAttackable())
			{
				foundMobs = true;
				_npcAnchors.computeIfAbsent(m.getLevel(), k -> new ArrayList<>()).add(new Location(m.getX(), m.getY(), m.getZ()));
				if (_npcAnchors.get(m.getLevel()).size() > 50)
				{
					_npcAnchors.get(m.getLevel()).remove(0);
				}
				
				boolean isTargetedByOtherPhantom = false;
				for (Player p : PhantomManager.activePhantoms)
				{
					if ((p != bot) && (p.getTarget() == m))
					{
						isTargetedByOtherPhantom = true;
						break;
					}
				}
				
				if (!isTargetedByOtherPhantom)
				{
					int lvlDiff = m.getLevel() - bot.getLevel();
					if ((lvlDiff >= -15) && (lvlDiff <= 5))
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
			stuckCount++;
			_stuckCounters.put(bot.getObjectId(), stuckCount);
			PhantomManager.logToFile(bot.getName(), "No encuentra mobs. Contador atascado: " + stuckCount);
			return;
		}
		
		_stuckCounters.put(bot.getObjectId(), 0);
		Monster target = !idealMobs.isEmpty() ? idealMobs.get(Rnd.get(idealMobs.size())) : (!backupMobs.isEmpty() ? backupMobs.get(Rnd.get(backupMobs.size())) : null);
		
		if (target != null)
		{
			PhantomManager.logToFile(bot.getName(), "Atacando a " + target.getName());
			bot.setTarget(target);
			executeAttackPlan(bot, target);
			return;
		}
		
		int newX = bot.getX(), newY = bot.getY();
		Player closePhantom = World.getInstance().getVisibleObjectsInRange(bot, Player.class, 400).stream().filter(p -> PhantomManager.activePhantoms.contains(p) && (p != bot)).findFirst().orElse(null);
		
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
		
		bot.getAI().setIntention(Intention.MOVE_TO, new Location(newX, newY, GeoEngine.getInstance().getHeight(newX, newY, bot.getZ())));
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
		
		if (!offensiveSkills.isEmpty() && (bot.getCurrentMp() > 50) && (Rnd.get(100) < 30))
		{
			bot.getAI().setIntention(Intention.CAST, offensiveSkills.get(Rnd.get(offensiveSkills.size())), target);
		}
		else
		{
			bot.getAI().setIntention(Intention.ATTACK, target);
		}
	}
}
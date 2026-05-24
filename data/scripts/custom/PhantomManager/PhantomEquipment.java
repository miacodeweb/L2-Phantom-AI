package custom.PhantomManager;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;

public class PhantomEquipment
{
	private static final int INVENTORY_CLEAN_LIMIT = 18;
	
	public static void checkProgression(Player bot)
	{
		PhantomProgression.checkProgression(bot);
		ensureShots(bot);
	}
	
	public static void cleanInventory(Player bot)
	{
		if (bot.getInventory().getSize() > INVENTORY_CLEAN_LIMIT)
		{
			int removed = 0;
			for (Item item : bot.getInventory().getItems())
			{
				if (!item.isEquipped() && (item.getId() != 57) && !isShot(item.getId()))
				{
					bot.destroyItem(ItemProcessType.DESTROY, item, bot, false);
					removed++;
				}
			}
			if (removed > 0)
			{
				PhantomManager.logToFile(bot.getName(), "Inventario limpiado automaticamente. Items removidos: " + removed);
			}
			bot.broadcastUserInfo();
		}
	}
	
	private static void ensureShots(Player bot)
	{
		int grade = PhantomState.GEAR_GRADE.getOrDefault(bot.getObjectId(), 0);
		if (grade < 0)
		{
			grade = 0;
		}
		if (grade >= PhantomConfig.FIGHTER_SHOTS.length)
		{
			grade = PhantomConfig.FIGHTER_SHOTS.length - 1;
		}
		
		int primaryShot = bot.isMageClass() ? PhantomConfig.MAGE_SHOTS[grade] : PhantomConfig.FIGHTER_SHOTS[grade];
		int secondaryShot = bot.isMageClass() ? PhantomConfig.FIGHTER_SHOTS[grade] : PhantomConfig.MAGE_SHOTS[grade];
		reloadShot(bot, primaryShot, Rnd.get(1800, 3600), true);
		if (Rnd.get(100) < 35)
		{
			reloadShot(bot, secondaryShot, Rnd.get(300, 900), false);
		}
	}
	
	private static void reloadShot(Player bot, int shotId, long amount, boolean autoUse)
	{
		if (shotId <= 0)
		{
			return;
		}
		if (bot.getInventory().getInventoryItemCount(shotId, -1) >= 250)
		{
			if (autoUse)
			{
				bot.addAutoSoulShot(shotId);
			}
			return;
		}
		
		bot.addItem(ItemProcessType.REWARD, shotId, amount, bot, false);
		bot.addAutoSoulShot(shotId);
		PhantomManager.logToFile(bot.getName(), "Recarga shots itemId=" + shotId + " cantidad=" + amount);
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
		
		applyKnownSelfBuffs(bot);
	}
	
	private static void applyKnownSelfBuffs(Player bot)
	{
		int objectId = bot.getObjectId();
		long now = System.currentTimeMillis();
		if ((now - PhantomState.LAST_SELF_BUFF.getOrDefault(objectId, 0L)) < 60000L)
		{
			return;
		}
		if ((bot.getCurrentMpPercent() < 35) || (bot.getEffectList().getBuffCount() >= 8))
		{
			return;
		}
		
		for (Skill skill : bot.getAllSkills())
		{
			if ((skill == null) || skill.isPassive() || skill.hasNegativeEffect())
			{
				continue;
			}
			
			String name = skill.getName().toLowerCase();
			if (name.contains("shield") || name.contains("might") || name.contains("focus") || name.contains("haste") || name.contains("empower") || name.contains("acumen") || name.contains("wind walk") || name.contains("concentration") || name.contains("vampiric") || name.contains("blessing") || name.contains("chant") || name.contains("song") || name.contains("dance"))
			{
				skill.applyEffects(bot, bot);
				PhantomState.LAST_SELF_BUFF.put(objectId, now + Rnd.get(15000, 45000));
				PhantomManager.logToFile(bot.getName(), "Usa buff propio: " + skill.getName());
				return;
			}
		}
	}
	
	static boolean isShot(int itemId)
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
}

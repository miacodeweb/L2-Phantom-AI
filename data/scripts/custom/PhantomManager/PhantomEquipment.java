package custom.PhantomManager;

import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;

public class PhantomEquipment
{
	public static void checkProgression(Player bot)
	{
		PhantomProgression.checkProgression(bot);
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
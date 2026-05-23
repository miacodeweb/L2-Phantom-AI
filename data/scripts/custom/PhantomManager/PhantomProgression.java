package custom.PhantomManager;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.enums.player.PlayerClass;
import org.l2jmobius.gameserver.model.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.model.item.instance.Item;

public class PhantomProgression
{
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
		if (targetGrade > PhantomState.GEAR_GRADE.getOrDefault(bot.getObjectId(), -1))
		{
			PhantomState.GEAR_GRADE.put(bot.getObjectId(), targetGrade);
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
}
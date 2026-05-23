package custom.PhantomManager;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.sql.CharInfoTable;
import org.l2jmobius.gameserver.data.xml.PlayerTemplateData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.appearance.PlayerAppearance;
import org.l2jmobius.gameserver.model.actor.enums.creature.Race;
import org.l2jmobius.gameserver.model.actor.enums.player.PlayerClass;
import org.l2jmobius.gameserver.model.actor.templates.PlayerTemplate;

public class PhantomFactory
{
	private static final String ACCOUNT_NAME = "phantom_ai";
	private static final PlayerClass[] BASE_CLASSES =
	{
		PlayerClass.FIGHTER,
		PlayerClass.MAGE,
		PlayerClass.ELVEN_FIGHTER,
		PlayerClass.ELVEN_MAGE,
		PlayerClass.DARK_FIGHTER,
		PlayerClass.DARK_MAGE,
		PlayerClass.ORC_FIGHTER,
		PlayerClass.ORC_MAGE,
		PlayerClass.DWARVEN_FIGHTER,
		PlayerClass.KAMAEL_SOLDIER,
		PlayerClass.SYLPH_GUNNER
	};
	private static final String[] FIRST_NAMES =
	{
		"Adrian",
		"Bruno",
		"Camila",
		"Daniel",
		"Elena",
		"Fabian",
		"Gabriel",
		"Hector",
		"Ivan",
		"Julian",
		"Laura",
		"Lucas",
		"Marcos",
		"Matias",
		"Nicolas",
		"Paula",
		"Rafael",
		"Sofia",
		"Tomas",
		"Valeria"
	};
	private static final String[] LAST_PARTS =
	{
		"Stone",
		"Blade",
		"River",
		"Storm",
		"Cross",
		"Vale",
		"Moon",
		"Steel",
		"Raven",
		"Frost",
		"Light",
		"Shade"
	};
	
	public static List<Player> createPhantoms(int count)
	{
		List<Player> created = new ArrayList<>();
		for (int i = 0; i < count; i++)
		{
			Player phantom = createOne();
			if (phantom != null)
			{
				created.add(phantom);
			}
		}
		return created;
	}
	
	private static Player createOne()
	{
		try
		{
			PlayerClass playerClass = getRandomAvailableBaseClass();
			if (playerClass == null)
			{
				return null;
			}
			
			PlayerTemplate template = PlayerTemplateData.getInstance().getTemplate(playerClass);
			String name = buildUniqueName();
			boolean female = Rnd.get(100) < 45;
			byte hairStyle = (byte) Rnd.get(female ? 12 : 9);
			byte hairColor = (byte) Rnd.get(4);
			byte face = (byte) Rnd.get(5);
			
			Player phantom = Player.create(template, ACCOUNT_NAME, name, new PlayerAppearance(face, hairColor, hairStyle, female));
			if (phantom == null)
			{
				return null;
			}
			
			phantom.setOnlineStatus(true, true);
			phantom.setRunning();
			phantom.setCurrentHp(phantom.getMaxHp());
			phantom.setCurrentMp(phantom.getMaxMp());
			phantom.storeMe();
			PhantomManager.logToFile(name, "Creado automaticamente. Raza: " + template.getRace());
			return phantom;
		}
		catch (Exception e)
		{
			PhantomManager.logToFile("AUTO_CREATE", "Error creando phantom: " + e.getMessage());
			return null;
		}
	}
	
	private static PlayerClass getRandomAvailableBaseClass()
	{
		List<PlayerClass> available = new ArrayList<>();
		for (PlayerClass playerClass : BASE_CLASSES)
		{
			PlayerTemplate template = PlayerTemplateData.getInstance().getTemplate(playerClass);
			if ((template != null) && isPlayableRace(template.getRace()))
			{
				available.add(playerClass);
			}
		}
		return available.isEmpty() ? null : available.get(Rnd.get(available.size()));
	}
	
	private static boolean isPlayableRace(Race race)
	{
		return (race == Race.HUMAN) || (race == Race.ELF) || (race == Race.DARK_ELF) || (race == Race.ORC) || (race == Race.DWARF) || (race == Race.KAMAEL) || (race == Race.SYLPH) || (race == Race.HIGH_ELF);
	}
	
	private static String buildUniqueName()
	{
		for (int i = 0; i < 100; i++)
		{
			String name = FIRST_NAMES[Rnd.get(FIRST_NAMES.length)] + LAST_PARTS[Rnd.get(LAST_PARTS.length)];
			if (name.length() > 16)
			{
				name = name.substring(0, 16);
			}
			if (!CharInfoTable.getInstance().doesCharNameExist(name))
			{
				return name;
			}
		}
		return "Phantom" + System.currentTimeMillis() % 100000000L;
	}
}

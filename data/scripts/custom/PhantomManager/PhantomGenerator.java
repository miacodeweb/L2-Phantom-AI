package custom.PhantomManager;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Random;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.managers.IdManager;
import org.l2jmobius.gameserver.model.actor.Player;

public class PhantomGenerator
{
	private static final int SPAWN_X = -84318;
	private static final int SPAWN_Y = 244579;
	private static final int SPAWN_Z = -3730;
	
	private static final int[] CLASSES =
	{
		0,
		10,
		18,
		25,
		31,
		38,
		44,
		49,
		53
	};
	private static final int[] RACES =
	{
		0,
		0,
		1,
		1,
		2,
		2,
		3,
		3,
		4
	};
	
	// Generador de Nombres de Fantasía
	private static final String[] SYL_1 =
	{
		"A",
		"Be",
		"Ca",
		"Da",
		"El",
		"Fa",
		"Ga",
		"He",
		"I",
		"Ka",
		"La",
		"Ma",
		"Na",
		"O",
		"Pa",
		"Ra",
		"Sa",
		"Ta",
		"Va",
		"Za"
	};
	private static final String[] SYL_2 =
	{
		"la",
		"me",
		"ni",
		"ro",
		"su",
		"da",
		"fe",
		"gi",
		"ko",
		"lu",
		"ma",
		"ne",
		"pi",
		"ra",
		"se",
		"to",
		"vu",
		"za"
	};
	private static final String[] SYL_3 =
	{
		"s",
		"n",
		"r",
		"l",
		"th",
		"x",
		"dor",
		"mar",
		"nis",
		"las",
		"mus",
		"ria",
		"tia",
		"nia",
		"wyn"
	};
	
	public static void createInGame(int cantidad, Player gm)
	{
		Random rnd = new Random();
		int creados = 0;
		String query = "INSERT IGNORE INTO `characters` (`charId`, `account_name`, `char_name`, `level`, `maxHp`, `curHp`, `maxCp`, `curCp`, `maxMp`, `curMp`, `face`, `hairStyle`, `hairColor`, `sex`, `x`, `y`, `z`, `race`, `classid`, `base_class`, `title`, `online`, `accesslevel`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			for (int i = 0; i < cantidad; i++)
			{
				int charId = IdManager.getInstance().getNextId();
				String charName = SYL_1[rnd.nextInt(SYL_1.length)] + SYL_2[rnd.nextInt(SYL_2.length)] + SYL_3[rnd.nextInt(SYL_3.length)];
				
				int classIndex = rnd.nextInt(CLASSES.length);
				int classId = CLASSES[classIndex];
				int race = RACES[classIndex];
				int sex = rnd.nextInt(2);
				
				ps.setInt(1, charId);
				ps.setString(2, "PhantomAuto");
				ps.setString(3, charName);
				ps.setInt(4, 1);
				ps.setInt(5, 1000);
				ps.setInt(6, 1000);
				ps.setInt(7, 500);
				ps.setInt(8, 500);
				ps.setInt(9, 1000);
				ps.setInt(10, 1000);
				ps.setInt(11, rnd.nextInt(3));
				ps.setInt(12, rnd.nextInt(4));
				ps.setInt(13, rnd.nextInt(3));
				ps.setInt(14, sex);
				ps.setInt(15, SPAWN_X);
				ps.setInt(16, SPAWN_Y);
				ps.setInt(17, SPAWN_Z);
				ps.setInt(18, race);
				ps.setInt(19, classId);
				ps.setInt(20, classId);
				ps.setString(21, "");
				ps.setInt(22, 0);
				ps.setInt(23, 0);
				
				ps.addBatch();
				PhantomConfig.PHANTOM_IDS.add(charId);
				creados++;
			}
			
			ps.executeBatch();
			saveXML();
			if (gm != null)
			{
				gm.sendMessage(">>> ¡Éxito! Se han creado " + creados + " Phantoms nuevos en la Base de Datos.");
			}
		}
		catch (Exception e)
		{
			if (gm != null)
			{
				gm.sendMessage("Error creando phantoms: " + e.getMessage());
			}
			e.printStackTrace();
		}
	}
	
	private static void saveXML()
	{
		try (FileWriter fw = new FileWriter("config/Custom/PhantomPlayers.xml");
			PrintWriter pw = new PrintWriter(fw))
		{
			pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<list>");
			for (int id : PhantomConfig.PHANTOM_IDS)
			{
				pw.println("\t<phantom charId=\"" + id + "\" />");
			}
			pw.println("</list>");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
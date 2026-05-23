package custom.PhantomManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PhantomHuntingSpots
{
	private static final Map<Integer, Integer> NPC_LEVELS = new HashMap<>();
	private static final Map<Integer, List<Location>> SPOTS_BY_LEVEL = new HashMap<>();
	private static boolean _loaded = false;
	
	public static synchronized void load()
	{
		if (_loaded)
		{
			return;
		}
		
		NPC_LEVELS.clear();
		SPOTS_BY_LEVEL.clear();
		loadNpcLevels(new File("data/stats/npcs"));
		loadSpawnSpots(new File("data/spawns"));
		_loaded = true;
		
		int total = 0;
		for (List<Location> spots : SPOTS_BY_LEVEL.values())
		{
			total += spots.size();
		}
		System.out.println(">>> [PHANTOM SYSTEM] Spots de leveo cargados desde datapack: " + total + " coordenadas en " + SPOTS_BY_LEVEL.size() + " niveles.");
	}
	
	public static boolean relocateForLevelIfNeeded(Player bot)
	{
		int band = getLevelBand(bot.getLevel());
		int objectId = bot.getObjectId();
		if (System.currentTimeMillis() < PhantomState.NEXT_HUNT_TELEPORT.getOrDefault(objectId, 0L))
		{
			return false;
		}
		if (PhantomState.HUNT_LEVEL_BAND.getOrDefault(objectId, -1) == band)
		{
			return false;
		}
		
		Location spot = getRandomSpot(bot.getLevel());
		if (spot == null)
		{
			return false;
		}
		
		PhantomState.HUNT_LEVEL_BAND.put(objectId, band);
		Location safe = PhantomGeo.getNpcLikeSpawn(spot);
		bot.teleToLocation(safe.getX(), safe.getY(), safe.getZ());
		bot.broadcastUserInfo();
		PhantomManager.logToFile(bot.getName(), "Viajando a spot de leveo L" + bot.getLevel() + ": " + safe.getX() + ", " + safe.getY() + ", " + safe.getZ());
		return true;
	}
	
	public static Location getRandomSpot(int playerLevel)
	{
		for (int range = 0; range <= 12; range++)
		{
			List<Location> candidates = new ArrayList<>();
			int minLevel = Math.max(1, playerLevel - 7 - range);
			int maxLevel = playerLevel + 2 + range;
			for (int level = minLevel; level <= maxLevel; level++)
			{
				List<Location> spots = SPOTS_BY_LEVEL.get(level);
				if (spots != null)
				{
					candidates.addAll(spots);
				}
			}
			if (!candidates.isEmpty())
			{
				return candidates.get(Rnd.get(candidates.size()));
			}
		}
		return null;
	}
	
	private static int getLevelBand(int level)
	{
		return Math.max(1, (level / 5) * 5);
	}
	
	private static void loadNpcLevels(File dir)
	{
		File[] files = dir.listFiles();
		if (files == null)
		{
			return;
		}
		
		for (File file : files)
		{
			if (file.isDirectory())
			{
				loadNpcLevels(file);
			}
			else if (file.getName().endsWith(".xml"))
			{
				parseNpcFile(file);
			}
		}
	}
	
	private static void parseNpcFile(File file)
	{
		try
		{
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			NodeList npcNodes = doc.getElementsByTagName("npc");
			for (int i = 0; i < npcNodes.getLength(); i++)
			{
				Element npc = (Element) npcNodes.item(i);
				if (!"Monster".equalsIgnoreCase(npc.getAttribute("type")))
				{
					continue;
				}
				
				NodeList statusNodes = npc.getElementsByTagName("status");
				if (statusNodes.getLength() == 0)
				{
					continue;
				}
				
				Element status = (Element) statusNodes.item(0);
				if (!"true".equalsIgnoreCase(status.getAttribute("attackable")))
				{
					continue;
				}
				
				int id = Integer.parseInt(npc.getAttribute("id"));
				int level = Integer.parseInt(npc.getAttribute("level"));
				NPC_LEVELS.put(id, level);
			}
		}
		catch (Exception e)
		{
			PhantomManager.logToFile("SPOT_LOADER", "Error leyendo NPCs " + file.getName() + ": " + e.getMessage());
		}
	}
	
	private static void loadSpawnSpots(File dir)
	{
		File[] files = dir.listFiles();
		if (files == null)
		{
			return;
		}
		
		for (File file : files)
		{
			if (file.isDirectory())
			{
				loadSpawnSpots(file);
			}
			else if (file.getName().endsWith(".xml") && !file.getName().toLowerCase().contains("raid"))
			{
				parseSpawnFile(file);
			}
		}
	}
	
	private static void parseSpawnFile(File file)
	{
		try
		{
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			NodeList npcNodes = doc.getElementsByTagName("npc");
			for (int i = 0; i < npcNodes.getLength(); i++)
			{
				Element npc = (Element) npcNodes.item(i);
				int id = Integer.parseInt(npc.getAttribute("id"));
				Integer level = NPC_LEVELS.get(id);
				if (level == null)
				{
					continue;
				}
				
				int x = Integer.parseInt(npc.getAttribute("x"));
				int y = Integer.parseInt(npc.getAttribute("y"));
				int z = Integer.parseInt(npc.getAttribute("z"));
				List<Location> spots = SPOTS_BY_LEVEL.computeIfAbsent(level, k -> new ArrayList<>());
				if (spots.size() < 350)
				{
					spots.add(new Location(x, y, z));
				}
			}
		}
		catch (Exception e)
		{
			PhantomManager.logToFile("SPOT_LOADER", "Error leyendo spawns " + file.getName() + ": " + e.getMessage());
		}
	}
}

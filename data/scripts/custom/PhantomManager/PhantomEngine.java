package custom.PhantomManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.actor.Player;

public class PhantomEngine
{
	public static final List<Player> activePhantoms = new CopyOnWriteArrayList<>();
	public static boolean isRunning = false;
	
	public static void startSystem(Player gm)
	{
		if (isRunning)
		{
			if (gm != null)
			{
				gm.sendMessage("El sistema ya esta corriendo.");
			}
			return;
		}
		
		PhantomManager.startLogSession("PHANTOM_START");
		isRunning = true;
		int loadedCount = 0;
		PhantomManager.logToFile("SYSTEM", "Iniciando sistema con " + PhantomConfig.PHANTOM_IDS.size() + " IDs configurados.");
		
		for (int charId : PhantomConfig.PHANTOM_IDS)
		{
			try
			{
				if (World.getInstance().getPlayer(charId) != null)
				{
					continue;
				}
				
				Player phantom = Player.load(charId);
				if (phantom == null)
				{
					continue;
				}
				
				preparePhantom(phantom, false);
				registerPhantom(phantom, Rnd.get(100) < 10, Rnd.get(100) < 3);
				startPhantomAI(phantom);
				PhantomManager.logToFile(phantom.getName(), "IA programada desde XML. online=" + phantom.isOnline() + " spawned=" + phantom.isSpawned() + " loc=" + phantom.getX() + "," + phantom.getY() + "," + phantom.getZ());
				loadedCount++;
			}
			catch (Exception e)
			{
				PhantomManager.logException("SPAWN", "Error al spawnear charId " + charId, e);
				e.printStackTrace();
			}
		}
		
		if (gm != null)
		{
			gm.sendMessage(">>> Sistema INICIADO. Se cargaron " + loadedCount + " bots.");
		}
	}
	
	public static void createAndStart(int count, Player gm)
	{
		PhantomManager.startLogSession("PHANTOM_CREATE_" + count);
		if (!isRunning)
		{
			isRunning = true;
		}
		
		int createdCount = 0;
		for (Player phantom : PhantomFactory.createPhantoms(count))
		{
			try
			{
				preparePhantom(phantom, true);
				registerPhantom(phantom, Rnd.get(100) < 17, Rnd.get(100) < 6);
				startPhantomAI(phantom);
				PhantomManager.logToFile(phantom.getName(), "IA programada desde creacion. online=" + phantom.isOnline() + " spawned=" + phantom.isSpawned() + " loc=" + phantom.getX() + "," + phantom.getY() + "," + phantom.getZ());
				createdCount++;
			}
			catch (Exception e)
			{
				PhantomManager.logException("AUTO_SPAWN", "Error iniciando phantom", e);
			}
		}
		
		if (gm != null)
		{
			gm.sendMessage("Se crearon e iniciaron " + createdCount + " phantoms automaticos.");
			PhantomManager.logToFile("AUTO_CREATE", "Se crearon " + createdCount + " phantoms automaticos.");
			PhantomMenu.showMenu(gm);
		}
	}
	
	public static void stopSystem(Player gm)
	{
		if (!isRunning)
		{
			return;
		}
		
		PhantomManager.logToFile("SYSTEM", "Deteniendo sistema. Phantoms activos=" + activePhantoms.size());
		isRunning = false;
		for (Player p : activePhantoms)
		{
			if (p != null)
			{
				p.deleteMe();
			}
		}
		
		activePhantoms.clear();
		PhantomState.clear();
		
		if (gm != null)
		{
			gm.sendMessage(">>> Sistema DETENIDO.");
		}
	}
	
	public static Player getPhantomByName(String name)
	{
		return activePhantoms.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}
	
	public static void movePhantomTo(Player phantom, Location loc, String reason)
	{
		movePhantomTo(phantom, loc, null, reason);
	}
	
	public static void movePhantomTo(Player phantom, Location loc, Instance instance, String reason)
	{
		if ((phantom == null) || (loc == null))
		{
			return;
		}
		
		phantom.abortAttack();
		phantom.abortCast();
		phantom.setTarget(null);
		phantom.setInstance(instance);
		phantom.setInvisible(false);
		phantom.setRunning();
		phantom.setXYZ(loc.getX(), loc.getY(), loc.getZ());
		if (loc.getHeading() != 0)
		{
			phantom.setHeading(loc.getHeading());
		}
		if (!phantom.isSpawned())
		{
			phantom.spawnMe(loc.getX(), loc.getY(), loc.getZ());
		}
		else
		{
			phantom.broadcastInfo();
		}
		phantom.broadcastUserInfo();
		PhantomManager.logToFile(phantom.getName(), reason + ": " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + " spawned=" + phantom.isSpawned());
	}
	
	private static void preparePhantom(Player phantom, boolean creationSpawn)
	{
		phantom.setOnlineStatus(true, true);
		phantom.setRunning();
		phantom.setInvul(false);
		phantom.setInvisible(false);
		phantom.setInstanceById(0);
		phantom.setEnteredWorld();
		if (creationSpawn)
		{
			spawnAtCreationPoint(phantom);
		}
		else
		{
			spawnPhantom(phantom);
		}
		phantom.broadcastUserInfo();
	}
	
	private static void registerPhantom(Player phantom, boolean aggressive, boolean pkMode)
	{
		if (!activePhantoms.contains(phantom))
		{
			activePhantoms.add(phantom);
		}
		
		PhantomState.register(phantom.getObjectId(), aggressive, pkMode);
		if (pkMode)
		{
			phantom.setPkKills(Math.max(1, phantom.getPkKills()));
			phantom.setReputation(-Rnd.get(360, 3600));
			phantom.updatePvpTitleAndColor(true);
			phantom.broadcastReputation();
			phantom.broadcastUserInfo();
		}
	}
	
	private static void spawnPhantom(Player phantom)
	{
		Location spot = PhantomHuntingSpots.getRandomSpot(phantom.getLevel());
		int spawnX;
		int spawnY;
		int spawnZ;
		if (spot != null)
		{
			Location safe = PhantomGeo.getNpcLikeSpawn(spot);
			spawnX = safe.getX();
			spawnY = safe.getY();
			spawnZ = safe.getZ();
		}
		else
		{
			PhantomConfig.FarmZone zone = PhantomConfig.getIdealZone(phantom.getLevel());
			Location safe = PhantomGeo.getNpcLikeSpawn(new Location(zone.townX, zone.townY, zone.townZ));
			spawnX = safe.getX();
			spawnY = safe.getY();
			spawnZ = safe.getZ();
		}
		
		if (phantom.isSpawned())
		{
			movePhantomTo(phantom, new Location(spawnX, spawnY, spawnZ), "Reposicionado en spot NPC");
		}
		else
		{
			phantom.spawnMe(spawnX, spawnY, spawnZ);
		}
		PhantomManager.logToFile(phantom.getName(), "Spawn visible en spot NPC: " + spawnX + ", " + spawnY + ", " + spawnZ);
	}
	
	private static void spawnAtCreationPoint(Player phantom)
	{
		Location point = phantom.getTemplate().getCreationPoint();
		Location safe = PhantomGeo.getNpcLikeSpawn(point);
		int spawnX = safe.getX();
		int spawnY = safe.getY();
		int spawnZ = safe.getZ();
		
		if (phantom.isSpawned())
		{
			movePhantomTo(phantom, new Location(spawnX, spawnY, spawnZ), "Reposicionado en ciudad de origen");
		}
		else
		{
			phantom.spawnMe(spawnX, spawnY, spawnZ);
		}
		PhantomState.HUNT_LEVEL_BAND.put(phantom.getObjectId(), -1);
		PhantomState.NEXT_HUNT_TELEPORT.put(phantom.getObjectId(), System.currentTimeMillis() + 60000L);
		PhantomManager.logToFile(phantom.getName(), "Nacido en ciudad de origen: " + spawnX + ", " + spawnY + ", " + spawnZ);
	}
	
	private static void teleportPhantomToOrigin(Player phantom)
	{
		Location point = phantom.getTemplate().getCreationPoint();
		Location safe = PhantomGeo.getNpcLikeSpawn(point);
		movePhantomTo(phantom, safe, "Revive en ciudad de origen");
		PhantomState.HUNT_LEVEL_BAND.put(phantom.getObjectId(), -1);
		PhantomState.NEXT_HUNT_TELEPORT.put(phantom.getObjectId(), System.currentTimeMillis() + 30000L);
	}
	
	public static void startPhantomAI(Player bot)
	{
		if ((bot == null) || !isRunning)
		{
			return;
		}
		
		ThreadPool.schedule(() ->
		{
			if (!isRunning)
			{
				return;
			}
			
			try
			{
				if (!bot.isSpawned() && !bot.isDead())
				{
					PhantomManager.logToFile(bot.getName(), "IA detecto phantom sin spawn. Reposicionando.");
					spawnPhantom(bot);
				}
				if (!bot.isDead())
				{
					traceAi(bot, "tick online=" + bot.isOnline() + " spawned=" + bot.isSpawned() + " moving=" + bot.isMoving() + " casting=" + bot.isCastingNow() + " attacking=" + bot.isAttackingNow() + " loc=" + bot.getX() + "," + bot.getY() + "," + bot.getZ());
					PhantomEquipment.applyBasicBuffs(bot);
					PhantomEquipment.checkProgression(bot);
					PhantomEquipment.cleanInventory(bot);
					PhantomAI.thinkAndFarm(bot);
				}
				else if (bot.isDead())
				{
					if (!PhantomState.REVIVING_PHANTOMS.add(bot.getObjectId()))
					{
						return;
					}
					
					PhantomChat.botReply(bot, null, PhantomChat.DEATH_MESSAGES[Rnd.get(PhantomChat.DEATH_MESSAGES.length)], false);
					ThreadPool.schedule(() ->
					{
						try
						{
							if (isRunning && bot.isOnline())
							{
								bot.doRevive();
								bot.setCurrentHp(bot.getMaxHp());
								bot.setCurrentMp(bot.getMaxMp());
								teleportPhantomToOrigin(bot);
								bot.broadcastUserInfo();
								PhantomState.MP_RECOVERY_STATE.put(bot.getObjectId(), false);
							}
						}
						finally
						{
							PhantomState.REVIVING_PHANTOMS.remove(bot.getObjectId());
						}
					}, 15000);
				}
			}
			catch (Exception e)
			{
				PhantomManager.logException(bot.getName(), "Error en bucle IA", e);
				e.printStackTrace();
			}
			finally
			{
				startPhantomAI(bot);
			}
		}, 4000);
	}
	
	private static void traceAi(Player bot, String message)
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

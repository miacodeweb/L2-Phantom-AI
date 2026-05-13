package custom.PhantomManager;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.handler.VoicedCommandHandler;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.script.Script;
import org.l2jmobius.gameserver.network.enums.ChatType;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class PhantomManager extends Script implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"pstart",
		"pstop",
		"pload",
		"pm",
		"pmenu",
		"pdebug"
	};
	public static final List<Player> activePhantoms = new ArrayList<>();
	
	private boolean _isRunning = false;
	private static boolean _debugMode = true;
	
	public PhantomManager()
	{
		PhantomConfig.init();
		VoicedCommandHandler.getInstance().registerHandler(this);
		
		System.out.println("##################################################");
		System.out.println(">>> [PHANTOM SYSTEM] V31 - Arquitectura Modular");
		System.out.println("##################################################");
	}
	
	public static void logToFile(String botName, String action)
	{
		if (!_debugMode)
		{
			return;
		}
		try (FileWriter fw = new FileWriter("log/PhantomManager.txt", true);
			PrintWriter pw = new PrintWriter(fw))
		{
			pw.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + botName + ": " + action);
		}
		catch (IOException e)
		{
		}
	}
	
	private void loadAllPhantoms(Player gm)
	{
		int loadedCount = 0;
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
					System.out.println(">>> [PHANTOM SYSTEM] El charId " + charId + " no existe.");
					continue;
				}
				
				phantom.setOnlineStatus(true, true);
				phantom.setRunning();
				
				PhantomConfig.FarmZone idealZone = PhantomConfig.getIdealZone(phantom.getLevel());
				int safeTownZ = GeoEngine.getInstance().getHeight(idealZone.townX, idealZone.townY, idealZone.townZ);
				phantom.spawnMe(idealZone.townX + Rnd.get(-50, 50), idealZone.townY + Rnd.get(-50, 50), safeTownZ);
				phantom.broadcastUserInfo();
				
				activePhantoms.add(phantom);
				PhantomAI._mpRecoveryState.put(phantom.getObjectId(), false);
				PhantomAI._phantomGearGrade.put(phantom.getObjectId(), -1);
				PhantomAI._stuckCounters.put(phantom.getObjectId(), 0);
				
				logToFile(phantom.getName(), "Conectado. Z Validada por Geodata: " + safeTownZ);
				startPhantomAI(phantom);
				loadedCount++;
			}
			catch (Exception e)
			{
				logToFile("SPAWN_ERROR", e.getMessage());
			}
		}
		
		if (gm != null)
		{
			gm.sendMessage(">>> Sistema INICIADO. Se cargaron " + loadedCount + " bots.");
		}
		System.out.println(">>> [PHANTOM SYSTEM] Activo con " + loadedCount + " phantoms.");
	}
	
	private void startPhantomAI(Player bot)
	{
		if ((bot == null) || !_isRunning)
		{
			return;
		}
		ThreadPool.schedule(() ->
		{
			if (!_isRunning)
			{
				return;
			}
			if (bot.isOnline() && !bot.isDead())
			{
				PhantomAI.applyBasicBuffs(bot);
				PhantomAI.checkProgression(bot);
				PhantomAI.cleanInventory(bot);
				PhantomAI.thinkAndFarm(bot);
				startPhantomAI(bot);
			}
			else if (bot.isDead())
			{
				logToFile(bot.getName(), "Muerto. Esperando respawn.");
				PhantomChat.botReply(bot, null, PhantomChat.DEATH_MESSAGES[Rnd.get(PhantomChat.DEATH_MESSAGES.length)], false);
				ThreadPool.schedule(() ->
				{
					if (_isRunning && bot.isOnline())
					{
						bot.doRevive();
						bot.setCurrentHp(bot.getMaxHp());
						bot.setCurrentMp(bot.getMaxMp());
						PhantomConfig.FarmZone idealZone = PhantomConfig.getIdealZone(bot.getLevel());
						int safeZ = GeoEngine.getInstance().getHeight(idealZone.townX, idealZone.townY, idealZone.townZ);
						bot.teleToLocation(idealZone.townX, idealZone.townY, safeZ);
						bot.broadcastUserInfo();
						PhantomAI._mpRecoveryState.put(bot.getObjectId(), false);
						startPhantomAI(bot);
					}
				}, 15000);
			}
		}, 4000);
	}
	
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		if (event.startsWith("pgo_"))
		{
			String targetName = event.substring(4).trim();
			Player p = activePhantoms.stream().filter(bot -> bot.getName().equalsIgnoreCase(targetName)).findFirst().orElse(null);
			if (p != null)
			{
				player.teleToLocation(p.getLocation());
			}
			else
			{
				player.sendMessage("Phantom no encontrado.");
			}
		}
		else if (event.startsWith("pbring_"))
		{
			String targetName = event.substring(7).trim();
			Player p = activePhantoms.stream().filter(bot -> bot.getName().equalsIgnoreCase(targetName)).findFirst().orElse(null);
			if (p != null)
			{
				p.teleToLocation(player.getLocation());
				p.broadcastUserInfo();
			}
			else
			{
				player.sendMessage("Phantom no encontrado.");
			}
		}
		else if (event.startsWith("pkill_"))
		{
			String targetName = event.substring(6).trim();
			Player p = activePhantoms.stream().filter(bot -> bot.getName().equalsIgnoreCase(targetName)).findFirst().orElse(null);
			if ((p != null) && !p.isDead())
			{
				p.doDie(player);
				player.sendMessage("Has matado a " + targetName);
			}
			else
			{
				player.sendMessage("Phantom no encontrado o muerto.");
			}
		}
		else if (event.equals("pdebug"))
		{
			_debugMode = !_debugMode;
			player.sendMessage("Logs TXT: " + (_debugMode ? "ENCENDIDO" : "APAGADO"));
		}
		return super.onEvent(event, npc, player);
	}
	
	@Override
	public boolean onCommand(String command, Player player, String target)
	{
		if (command.equalsIgnoreCase("pstart"))
		{
			if (!_isRunning)
			{
				_isRunning = true;
				loadAllPhantoms(player);
			}
			else
			{
				player.sendMessage("El sistema ya esta corriendo.");
			}
			return true;
		}
		else if (command.equalsIgnoreCase("pstop"))
		{
			if (_isRunning)
			{
				_isRunning = false;
				for (Player p : activePhantoms)
				{
					if (p != null)
					{
						p.deleteMe();
					}
				}
				activePhantoms.clear();
				PhantomAI._mpRecoveryState.clear();
				PhantomAI._phantomGearGrade.clear();
				PhantomAI._stuckCounters.clear();
				PhantomAI._npcAnchors.clear();
				player.sendMessage("Sistema DETENIDO.");
				System.out.println(">>> [PHANTOM SYSTEM] Sistema Detenido.");
			}
			return true;
		}
		else if (command.equalsIgnoreCase("pload"))
		{
			PhantomConfig.loadXML();
			player.sendMessage("XML de Phantoms recargado.");
			return true;
		}
		else if (command.equalsIgnoreCase("pm"))
		{
			if ((target == null) || target.trim().isEmpty())
			{
				return true;
			}
			String[] parts = target.split(" ", 2);
			if (parts.length < 2)
			{
				return true;
			}
			String phantomName = parts[0];
			String message = parts[1];
			
			Player targetPhantom = activePhantoms.stream().filter(p -> p.getName().equalsIgnoreCase(phantomName)).findFirst().orElse(null);
			
			if (targetPhantom != null)
			{
				player.sendPacket(new CreatureSay(player, ChatType.WHISPER, "->" + targetPhantom.getName(), message));
				PhantomChat.requestGemini(targetPhantom, player, message, true);
			}
			else
			{
				player.sendMessage("El Phantom no esta farmeando.");
			}
			return true;
		}
		else if (command.equalsIgnoreCase("pmenu"))
		{
			if (!player.isGM())
			{
				return false;
			}
			StringBuilder html = new StringBuilder();
			html.append("<html><title>Phantom Manager</title><body><center><font color=\"LEVEL\">Panel de Control de Phantoms</font></center><br><table width=280>");
			if (activePhantoms.isEmpty())
			{
				html.append("<tr><td><center>El sistema esta apagado o no hay bots.</center></td></tr>");
			}
			else
			{
				for (Player p : activePhantoms)
				{
					html.append("<tr><td width=100>").append(p.getName()).append(" (L").append(p.getLevel()).append(")</td>");
					html.append("<td width=50><button value=\"Ir\" action=\"bypass -h Quest PhantomManager pgo_").append(p.getName()).append("\" width=45 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
					html.append("<td width=50><button value=\"Traer\" action=\"bypass -h Quest PhantomManager pbring_").append(p.getName()).append("\" width=45 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
					html.append("<td width=50><button value=\"Matar\" action=\"bypass -h Quest PhantomManager pkill_").append(p.getName()).append("\" width=45 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
				}
			}
			html.append("</table><br><center><button value=\"Activar/Desactivar Logs TXT\" action=\"bypass -h Quest PhantomManager pdebug\" width=180 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></body></html>");
			NpcHtmlMessage msg = new NpcHtmlMessage();
			msg.setHtml(html.toString());
			player.sendPacket(msg);
			return true;
		}
		return false;
	}
	
	@Override
	public String[] getCommandList()
	{
		return VOICED_COMMANDS;
	}
	
	public static void main(String[] args)
	{
		new PhantomManager();
	}
}
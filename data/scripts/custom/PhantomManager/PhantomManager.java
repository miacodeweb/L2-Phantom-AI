package custom.PhantomManager;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.handler.VoicedCommandHandler;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.script.Script;
import org.l2jmobius.gameserver.network.enums.ChatType;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;

public class PhantomManager extends Script implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"pstart",
		"pstop",
		"pload",
		"pcreate",
		"pstop10",
		"pm",
		"pmenu",
		"pdebug"
	};
	
	private static boolean _debugMode = true;
	private static String _sessionLogFile = "log/PhantomManager-session-pending.txt";
	
	public PhantomManager()
	{
		startLogSession("SERVER_START");
		PhantomConfig.init();
		VoicedCommandHandler.getInstance().registerHandler(this);
		PhantomBypass.register();
		
		System.out.println("##################################################");
		System.out.println(">>> [PHANTOM SYSTEM] V32 - Arquitectura Modular");
		System.out.println("##################################################");
	}
	
	public static void startLogSession(String reason)
	{
		_sessionLogFile = "log/PhantomManager-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".txt";
		logToFile("SYSTEM", "Nueva sesion de logs: " + reason);
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
		try (FileWriter fw = new FileWriter(_sessionLogFile, true);
			PrintWriter pw = new PrintWriter(fw))
		{
			pw.println("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + botName + ": " + action);
		}
		catch (IOException e)
		{
		}
	}
	
	public static void logException(String botName, String action, Exception e)
	{
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		logToFile(botName, action + ": " + e.getMessage() + System.lineSeparator() + sw);
	}
	
	public static boolean toggleDebug()
	{
		_debugMode = !_debugMode;
		return _debugMode;
	}
	
	public static boolean isDebugMode()
	{
		return _debugMode;
	}
	
	@Override
	public String onEvent(String event, Npc npc, Player player)
	{
		if ((player == null) || !player.isGM())
		{
			return super.onEvent(event, npc, player);
		}
		
		if (event.startsWith("pgo_"))
		{
			String targetName = event.substring(4).trim();
			Player p = PhantomEngine.getPhantomByName(targetName);
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
			Player p = PhantomEngine.getPhantomByName(targetName);
			if (p != null)
			{
				PhantomEngine.movePhantomTo(p, new org.l2jmobius.gameserver.model.Location(player.getX(), player.getY(), player.getZ(), player.getHeading()), player.getInstanceWorld(), "Traido por GM");
				player.sendMessage("Trajiste a " + p.getName() + ".");
			}
			else
			{
				player.sendMessage("Phantom no encontrado.");
			}
		}
		else if (event.startsWith("pkill_"))
		{
			String targetName = event.substring(6).trim();
			Player p = PhantomEngine.getPhantomByName(targetName);
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
		else if (event.startsWith("pspawn_"))
		{
			int count = Integer.parseInt(event.substring(7));
			if ((count == 10) || (count == 50))
			{
				PhantomEngine.createAndStart(count, player);
			}
		}
		else if (event.equals("pdebug"))
		{
			player.sendMessage("Logs TXT: " + (toggleDebug() ? "ENCENDIDO" : "APAGADO"));
		}
		
		return super.onEvent(event, npc, player);
	}
	
	@Override
	public boolean onCommand(String command, Player player, String target)
	{
		if (command.equalsIgnoreCase("pstart"))
		{
			PhantomEngine.startBatch(10, player);
			return true;
		}
		else if (command.equalsIgnoreCase("pstop"))
		{
			PhantomEngine.stopSystem(player);
			return true;
		}
		else if (command.equalsIgnoreCase("pstop10"))
		{
			PhantomEngine.stopSome(10, player);
			return true;
		}
		else if (command.equalsIgnoreCase("pload"))
		{
			PhantomConfig.loadXML();
			player.sendMessage("XML de Phantoms recargado.");
			return true;
		}
		else if (command.equalsIgnoreCase("pcreate"))
		{
			int count = 10;
			if ((target != null) && !target.trim().isEmpty())
			{
				try
				{
					count = Math.max(1, Math.min(100, Integer.parseInt(target.trim())));
				}
				catch (NumberFormatException e)
				{
					player.sendMessage("Uso: .pcreate 10");
					return true;
				}
			}
			PhantomEngine.createAndStart(count, player);
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
			
			Player targetPhantom = PhantomEngine.getPhantomByName(parts[0]);
			if (targetPhantom != null)
			{
				player.sendPacket(new CreatureSay(player, ChatType.WHISPER, "->" + targetPhantom.getName(), parts[1]));
				PhantomChat.requestGemini(targetPhantom, player, parts[1], true);
			}
			else
			{
				player.sendMessage("El Phantom no esta farmeando.");
			}
			return true;
		}
		else if (command.equalsIgnoreCase("pmenu"))
		{
			PhantomMenu.showMenu(player);
			return true;
		}
		else if (command.equalsIgnoreCase("pdebug"))
		{
			player.sendMessage("Logs TXT: " + (toggleDebug() ? "ENCENDIDO" : "APAGADO"));
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

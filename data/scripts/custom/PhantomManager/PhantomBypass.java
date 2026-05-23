package custom.PhantomManager;

import org.l2jmobius.gameserver.handler.BypassHandler;
import org.l2jmobius.gameserver.handler.IBypassHandler;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;

public class PhantomBypass implements IBypassHandler
{
	private static final PhantomBypass INSTANCE = new PhantomBypass();
	private static final String[] COMMANDS =
	{
		"phantom_create_10",
		"phantom_create_50",
		"phantom_go",
		"phantom_bring",
		"phantom_kill",
		"phantom_debug",
		"phantom_menu"
	};
	private static boolean _registered = false;
	
	public static void register()
	{
		if (!_registered)
		{
			BypassHandler.getInstance().registerHandler(INSTANCE);
			_registered = true;
		}
	}
	
	@Override
	public boolean onCommand(String command, Player player, Creature bypassOrigin)
	{
		if ((player == null) || !player.isGM())
		{
			return false;
		}
		
		if (command.equals("phantom_create_10"))
		{
			PhantomEngine.createAndStart(10, player);
			return true;
		}
		else if (command.equals("phantom_create_50"))
		{
			PhantomEngine.createAndStart(50, player);
			return true;
		}
		else if (command.startsWith("phantom_go "))
		{
			Player target = PhantomEngine.getPhantomByName(command.substring(11).trim());
			if (target != null)
			{
				player.teleToLocation(target.getLocation());
			}
			return true;
		}
		else if (command.startsWith("phantom_bring "))
		{
			Player target = PhantomEngine.getPhantomByName(command.substring(14).trim());
			if (target != null)
			{
				target.teleToLocation(player.getLocation());
				target.broadcastUserInfo();
			}
			return true;
		}
		else if (command.startsWith("phantom_kill "))
		{
			Player target = PhantomEngine.getPhantomByName(command.substring(13).trim());
			if ((target != null) && !target.isDead())
			{
				target.doDie(player);
			}
			return true;
		}
		else if (command.equals("phantom_debug"))
		{
			player.sendMessage("Logs TXT: " + (PhantomManager.toggleDebug() ? "ENCENDIDO" : "APAGADO"));
			return true;
		}
		else if (command.equals("phantom_menu"))
		{
			PhantomMenu.showMenu(player);
			return true;
		}
		return false;
	}
	
	@Override
	public String[] getCommandList()
	{
		return COMMANDS;
	}
}

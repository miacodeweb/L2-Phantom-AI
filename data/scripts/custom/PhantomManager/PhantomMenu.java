package custom.PhantomManager;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class PhantomMenu
{
	private static final int MAX_VISIBLE_PHANTOMS = 18;
	
	public static void showMenu(Player player)
	{
		if ((player == null) || !player.isGM())
		{
			return;
		}
		
		StringBuilder html = new StringBuilder();
		html.append("<html><title>Phantom Manager</title><body><center><font color=\"LEVEL\">Panel de Control de Phantoms</font></center><br>");
		html.append("<center><table width=280><tr>");
		html.append("<td><button value=\"Crear 10\" action=\"bypass -h phantom_create_10\" width=85 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		html.append("<td><button value=\"Crear 50\" action=\"bypass -h phantom_create_50\" width=85 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		html.append("</tr></table></center><br><table width=280>");
		
		if (PhantomEngine.activePhantoms.isEmpty())
		{
			html.append("<tr><td><center>El sistema esta apagado o no hay bots.</center></td></tr>");
		}
		else
		{
			int count = 0;
			for (Player p : PhantomEngine.activePhantoms)
			{
				if (count >= MAX_VISIBLE_PHANTOMS)
				{
					break;
				}
				html.append("<tr><td width=100>").append(p.getName()).append(" (L").append(p.getLevel()).append(")</td>");
				html.append("<td width=50><button value=\"Ir\" action=\"bypass -h phantom_go ").append(p.getName()).append("\" width=45 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				html.append("<td width=50><button value=\"Traer\" action=\"bypass -h phantom_bring ").append(p.getName()).append("\" width=45 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				html.append("<td width=50><button value=\"Matar\" action=\"bypass -h phantom_kill ").append(p.getName()).append("\" width=45 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
				count++;
			}
			if (PhantomEngine.activePhantoms.size() > MAX_VISIBLE_PHANTOMS)
			{
				html.append("<tr><td><center>Mostrando ").append(MAX_VISIBLE_PHANTOMS).append(" de ").append(PhantomEngine.activePhantoms.size()).append(" phantoms.</center></td></tr>");
			}
		}
		
		html.append("</table><br><center><button value=\"Activar/Desactivar Logs TXT\" action=\"bypass -h phantom_debug\" width=180 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></body></html>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage();
		msg.setHtml(html.toString());
		player.sendPacket(msg);
	}
}

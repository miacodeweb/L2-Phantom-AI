package custom.PhantomManager;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class PhantomMenu
{
	public static void showMenu(Player player)
	{
		if ((player == null) || !player.isGM())
		{
			return;
		}
		
		StringBuilder html = new StringBuilder();
		html.append("<html><title>Phantom Manager</title><body><center><font color=\"LEVEL\">Panel de Control de Phantoms</font></center><br>");
		html.append("<center>Activos: ").append(PhantomEngine.activePhantoms.size()).append(" / XML: ").append(PhantomConfig.PHANTOM_IDS.size()).append("</center><br>");
		html.append("<center>Logs TXT: ").append(PhantomManager.isDebugMode() ? "<font color=\"00FF00\">ACTIVO</font>" : "<font color=\"FF5555\">INACTIVO</font>").append("</center><br>");
		html.append("<center><table width=280>");
		html.append("<tr>");
		html.append("<td><button value=\"Iniciar 10\" action=\"bypass -h phantom_start_10\" width=90 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		html.append("<td><button value=\"Desconectar 10\" action=\"bypass -h phantom_stop_10\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		html.append("</tr><tr>");
		html.append("<td><button value=\"Crear 10\" action=\"bypass -h phantom_create_10\" width=90 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		html.append("<td><button value=\"Crear 50\" action=\"bypass -h phantom_create_50\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		html.append("</tr><tr>");
		html.append("<td><button value=\"Recargar XML\" action=\"bypass -h phantom_reload_xml\" width=90 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		html.append("<td><button value=\"Detener Todo\" action=\"bypass -h phantom_stop_all\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		html.append("</tr><tr>");
		html.append("<td></td>");
		html.append("<td><button value=\"").append(PhantomManager.isDebugMode() ? "Desactivar Log" : "Activar Log").append("\" action=\"bypass -h phantom_debug\" width=120 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		html.append("</tr>");
		html.append("</table></center><br>");
		html.append("<center>Comandos: .pstart carga 10, .pstop detiene todo, .pcreate 10 crea y guarda.</center>");
		html.append("</body></html>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage();
		msg.setHtml(html.toString());
		player.sendPacket(msg);
	}
}

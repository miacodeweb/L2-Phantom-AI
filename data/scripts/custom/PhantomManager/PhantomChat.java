package custom.PhantomManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.enums.ChatType;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;

public class PhantomChat
{
	private static final String GEMINI_API_KEY = "TU_API_KEY_AQUI";
	public static final String[] DEATH_MESSAGES =
	{
		"lag...",
		"wtf de donde salio ese",
		"omg mi equipo",
		"rez pls"
	};
	private static final String[] RANDOM_MESSAGES =
	{
		"alguien para rb?",
		"vendo mats md",
		"necesito buff",
		"cuanta adena cae aca..."
	};
	
	public static void botReply(Player bot, Player sender, String text, boolean isPrivate)
	{
		if (isPrivate && (sender != null))
		{
			sender.sendPacket(new CreatureSay(bot, ChatType.WHISPER, bot.getName(), text));
		}
		else
		{
			bot.broadcastPacket(new CreatureSay(bot, ChatType.GENERAL, bot.getName(), text));
		}
	}
	
	public static void requestGemini(Player bot, Player sender, String prompt, boolean isPrivate)
	{
		if (GEMINI_API_KEY.contains("TU_API_KEY_AQUI") || GEMINI_API_KEY.isEmpty())
		{
			botReply(bot, sender, RANDOM_MESSAGES[Rnd.get(RANDOM_MESSAGES.length)], isPrivate);
			return;
		}
		CompletableFuture.runAsync(() ->
		{
			try
			{
				String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + GEMINI_API_KEY;
				String cleanPrompt = prompt.replace("\"", "'").replace("\\", "").replace("\n", " ");
				String jsonPayload = "{\"contents\": [{\"parts\": [{\"text\": \"Eres un jugador del mmorpg Lineage 2. Habla corto, maximo 15 palabras. El jugador dice: " + cleanPrompt + "\"}]}],\"generationConfig\": {\"temperature\": 0.7, \"maxOutputTokens\": 30}}";
				
				HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
				HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(jsonPayload)).build();
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				Matcher matcher = Pattern.compile("\"text\"\\s*:\\s*\"(.*?)\"").matcher(response.body());
				
				if (matcher.find())
				{
					botReply(bot, sender, matcher.group(1).replace("\\n", " ").replace("\\\"", "\"").trim(), isPrivate);
				}
			}
			catch (Exception e)
			{
				PhantomManager.logToFile("GEMINI", "Error: " + e.getMessage());
			}
		});
	}
}
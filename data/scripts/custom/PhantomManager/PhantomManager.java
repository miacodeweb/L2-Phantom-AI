package custom.PhantomManager;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.Intention;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.handler.VoicedCommandHandler;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.enums.player.PlayerClass;
import org.l2jmobius.gameserver.model.actor.enums.player.PlayerAction;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.item.enums.ShotType;
import org.l2jmobius.gameserver.model.item.enums.ItemProcessType;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.script.Script;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.enums.ChatType;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class PhantomManager extends Script implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS = { "pstart", "pstop", "pload", "pm", "pmenu", "pgo", "pbring", "pkill" };
	private static final List<Integer> PHANTOM_IDS = new ArrayList<>();
	
	// API KEY DE GEMINI (Reemplaza con tu clave de Google AI Studio)
	private static final String GEMINI_API_KEY = "TU_API_KEY_AQUI"; 
	
	private static final String[] DEATH_MESSAGES = { "lag...", "wtf de donde salio ese", "omg mi equipo", "rez pls", "que asco de bicho" };
	private static final String[] RANDOM_MESSAGES = { "alguien para rb?", "vendo mats md", "necesito buff", "aburridoooo", "cuanta adena cae aca..." };

	private static class FarmZone
	{
		int minLvl, maxLvl;
		int spotX, spotY, spotZ;
		int townX, townY, townZ;
		
		FarmZone(int min, int max, int sx, int sy, int sz, int tx, int ty, int tz)
		{
			this.minLvl = min; this.maxLvl = max;
			this.spotX = sx; this.spotY = sy; this.spotZ = sz;
			this.townX = tx; this.townY = ty; this.townZ = tz;
		}
	}
	
	private static final List<FarmZone> ROUTES = new ArrayList<>();
	private static final List<Player> activePhantoms = new ArrayList<>();
	
	private static final Map<Integer, Boolean> _mpRecoveryState = new ConcurrentHashMap<>();
	private static final Map<Integer, Integer> _phantomGearGrade = new ConcurrentHashMap<>();
	private static final Map<Integer, Location> _lastLocations = new ConcurrentHashMap<>();
	private static final Map<Integer, Integer> _stuckCounters = new ConcurrentHashMap<>();
	
	private boolean _isRunning = false;

	private static final int[][] FIGHTER_GEAR = { { 19, 23, 42 }, { 28, 2386, 2382 }, { 122, 338, 356 }, { 74, 2381, 2380 }, { 80, 2376 }, { 6580, 6373, 6374 } };
	private static final int[][] MAGE_GEAR = { { 17, 46, 47 }, { 1120, 439, 471 }, { 1224, 438, 470 }, { 1229, 2406 }, { 1230, 2407 }, { 6608, 2409 } };

	private static final int[] FIGHTER_SHOTS = { 1463, 1464, 1465, 1466, 1467, 1462 };
	private static final int[] MAGE_SHOTS = { 3947, 3948, 3949, 3950, 3951, 3952 };

	public PhantomManager()
	{
		ROUTES.add(new FarmZone(1, 15, -75291, 251836, -2700, -84318, 244579, -3730));         
		ROUTES.add(new FarmZone(15, 25, -43295, 118875, -2600, -14225, 123540, -3121)); 
		ROUTES.add(new FarmZone(25, 35, 33924, 134785, -2500, 15670, 142983, -2705));      
		ROUTES.add(new FarmZone(35, 45, 47214, 147572, -2000, 15670, 142983, -2705));      
		ROUTES.add(new FarmZone(45, 55, 138584, 9639, -3500, 146142, 26715, -2200));        
		ROUTES.add(new FarmZone(55, 65, 109240, 40968, -4000, 117110, 76883, -2695));       
		ROUTES.add(new FarmZone(65, 75, 140406, 12433, -3400, 146142, 26715, -2200));        
		ROUTES.add(new FarmZone(75, 80, 172310, 52740, -4800, 146142, 26715, -2200));        
		ROUTES.add(new FarmZone(80, 85, 110000, 118000, -2500, 83400, 147943, -3404));       
		ROUTES.add(new FarmZone(85, 100, -55680, 136162, -2200, -13973, 122208, -3116));     

		VoicedCommandHandler.getInstance().registerHandler(this);
		
		System.out.println("##################################################");
		System.out.println(">>> [PHANTOM SYSTEM] V22 - Ready for GitHub");
		System.out.println("##################################################");
		
		loadXML();
		
		ThreadPool.schedule(() -> 
		{
			if (!_isRunning)
			{
				_isRunning = true;
				loadAllPhantoms();
			}
		}, 10000);
	}

	private void botSay(Player bot, String text)
	{
		bot.broadcastPacket(new CreatureSay(bot, ChatType.GENERAL, bot.getName(), text));
	}

	private void botReply(Player bot, Player sender, String text, boolean isPrivate)
	{
		if (isPrivate && sender != null) sender.sendPacket(new CreatureSay(bot, ChatType.WHISPER, bot.getName(), text));
		else bot.broadcastPacket(new CreatureSay(bot, ChatType.GENERAL, bot.getName(), text));
	}

	private void requestGemini(Player bot, Player sender, String prompt, boolean isPrivate)
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
				String contexto = "Eres un jugador del mmorpg Lineage 2. Habla muy corto, relajado, con slang gamer, maximo 15 palabras. El jugador dice: " + cleanPrompt;
				
				String jsonPayload = "{\"contents\": [{\"parts\": [{\"text\": \"" + contexto + "\"}]}],\"generationConfig\": {\"temperature\": 0.7, \"maxOutputTokens\": 30}}";

				HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
				HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(jsonPayload)).build();

				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				Matcher matcher = Pattern.compile("\"text\"\\s*:\\s*\"(.*?)\"").matcher(response.body());
				
				if (matcher.find()) botReply(bot, sender, matcher.group(1).replace("\\n", " ").replace("\\\"", "\"").trim(), isPrivate);
				else botReply(bot, sender, RANDOM_MESSAGES[Rnd.get(RANDOM_MESSAGES.length)], isPrivate);
			}
			catch (Exception e) { System.out.println(">>> [PHANTOM AI] Error HTTP Gemini: " + e.getMessage()); }
		});
	}

	private boolean isShot(int itemId)
	{
		for (int id : FIGHTER_SHOTS) if (id == itemId) return true;
		for (int id : MAGE_SHOTS) if (id == itemId) return true;
		return false;
	}

	private void cleanInventory(Player bot)
	{
		if (bot.getInventory().getSize() > 10)
		{
			for (Item item : bot.getInventory().getItems())
			{
				if (!item.isEquipped() && item.getId() != 57 && !isShot(item.getId())) 
				{
					bot.destroyItem(ItemProcessType.DESTROY, item, bot, false);
				}
			}
			bot.broadcastUserInfo();
		}
	}

	private void loadXML()
	{
		PHANTOM_IDS.clear();
		try
		{
			File file = new File("config/Custom/PhantomPlayers.xml");
			if (!file.exists()) return;
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			NodeList list = doc.getElementsByTagName("phantom");
			for (int i = 0; i < list.getLength(); i++)
			{
				PHANTOM_IDS.add(Integer.parseInt(list.item(i).getAttributes().getNamedItem("charId").getNodeValue()));
			}
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	private void loadAllPhantoms()
	{
		for (int charId : PHANTOM_IDS)
		{
			try
			{
				if (World.getInstance().getPlayer(charId) != null) continue;
				Player phantom = Player.load(charId);
				if (phantom == null) continue;

				phantom.setOnlineStatus(true, true);
				phantom.setRunning(); 
				
				FarmZone idealZone = getIdealZone(phantom.getLevel());
				
				phantom.spawnMe(idealZone.townX + Rnd.get(-50, 50), idealZone.townY + Rnd.get(-50, 50), idealZone.townZ);
				phantom.broadcastUserInfo();
				
				activePhantoms.add(phantom);
				_mpRecoveryState.put(phantom.getObjectId(), false);
				_phantomGearGrade.put(phantom.getObjectId(), -1); 
				_stuckCounters.put(phantom.getObjectId(), 0);
				
				startPhantomAI(phantom);
			}
			catch (Exception e) { e.printStackTrace(); }
		}
	}

	private void startPhantomAI(Player bot)
	{
		if (bot == null || !_isRunning) return;

		ThreadPool.schedule(() -> 
		{
			if (!_isRunning) return;

			if (bot.isOnline() && !bot.isDead())
			{
				applyBasicBuffs(bot);
				checkProgression(bot); 
				cleanInventory(bot); 
				
				FarmZone idealZone = getIdealZone(bot.getLevel());
				
				thinkAndFarm(bot, idealZone);
				startPhantomAI(bot);
			}
			else if (bot.isDead())
			{
				botReply(bot, null, DEATH_MESSAGES[Rnd.get(DEATH_MESSAGES.length)], false);

				ThreadPool.schedule(() -> 
				{
					if (_isRunning)
					{
						bot.doRevive();
						bot.setCurrentHp(bot.getMaxHp());
						bot.setCurrentMp(bot.getMaxMp());
						FarmZone idealZone = getIdealZone(bot.getLevel());
						bot.teleToLocation(idealZone.townX + Rnd.get(-50, 50), idealZone.townY + Rnd.get(-50, 50), idealZone.townZ);
						bot.broadcastUserInfo();
						_mpRecoveryState.put(bot.getObjectId(), false);
						startPhantomAI(bot);
					}
				}, 15000);
			}
		}, 3000); 
	}

	private void checkProgression(Player bot)
	{
		int lvl = bot.getLevel();
		PlayerClass currentClass = bot.getPlayerClass();
		int classLevel = currentClass.level(); 
		boolean needsClassChange = (lvl >= 20 && classLevel == 0) || (lvl >= 40 && classLevel == 1) || (lvl >= 76 && classLevel == 2) || (lvl >= 85 && classLevel == 3);

		if (needsClassChange)
		{
			List<PlayerClass> nextClasses = new ArrayList<>();
			for (PlayerClass cid : PlayerClass.values()) { if (cid.getParent() == currentClass) nextClasses.add(cid); }
			if (!nextClasses.isEmpty())
			{
				PlayerClass newClass = nextClasses.get(Rnd.get(nextClasses.size()));
				bot.setPlayerClass(newClass.getId()); 
				bot.setBaseClass(newClass.getId());
				bot.broadcastUserInfo();
			}
		}

		int targetGrade = 0; 
		if (lvl >= 20 && lvl < 40) targetGrade = 1; 
		else if (lvl >= 40 && lvl < 52) targetGrade = 2; 
		else if (lvl >= 52 && lvl < 61) targetGrade = 3; 
		else if (lvl >= 61 && lvl < 76) targetGrade = 4; 
		else if (lvl >= 76) targetGrade = 5; 

		int currentGrade = _phantomGearGrade.getOrDefault(bot.getObjectId(), -1);
		if (targetGrade > currentGrade)
		{
			_phantomGearGrade.put(bot.getObjectId(), targetGrade);
			equipPhantom(bot, targetGrade);
		}

		int shotId = bot.isMageClass() ? MAGE_SHOTS[targetGrade] : FIGHTER_SHOTS[targetGrade];
		if (bot.getInventory().getInventoryItemCount(shotId, -1) < 100)
		{
			bot.addItem(ItemProcessType.REWARD, shotId, 1000, bot, false);
			bot.addAutoSoulShot(shotId);
			bot.broadcastUserInfo();
		}
	}

	private void equipPhantom(Player bot, int grade)
	{
		int[] gearSet = bot.isMageClass() ? MAGE_GEAR[grade] : FIGHTER_GEAR[grade];
		for (int itemId : gearSet)
		{
			Item item = bot.getInventory().getItemByItemId(itemId);
			if (item == null) item = bot.addItem(ItemProcessType.REWARD, itemId, 1L, bot, false);
			if (item != null && !item.isEquipped()) bot.getInventory().equipItem(item);
		}
		bot.broadcastUserInfo();
	}

	private FarmZone getIdealZone(int level)
	{
		for (FarmZone zone : ROUTES) { if (level >= zone.minLvl && level < zone.maxLvl) return zone; }
		return ROUTES.get(ROUTES.size() - 1); 
	}

	private void applyBasicBuffs(Player bot)
	{
		if (bot.getEffectList().getBuffCount() < 3)
		{
			int[] buffIds = { 1086, 1204, 1068 }; 
			for (int id : buffIds)
			{
				Skill buff = SkillData.getInstance().getSkill(id, 2); 
				if (buff != null) buff.applyEffects(bot, bot);
			}
		}
	}

	private void thinkAndFarm(Player bot, FarmZone zone)
	{
		Location spotLoc = new Location(zone.spotX, zone.spotY, zone.spotZ);
		double distanceToSpot = bot.calculateDistance2D(spotLoc);

		if (distanceToSpot > 6000)
		{
			int stuckCount = _stuckCounters.getOrDefault(bot.getObjectId(), 0);
			stuckCount++;
			_stuckCounters.put(bot.getObjectId(), stuckCount);

			if (stuckCount == 1)
			{
				bot.getAI().setIntention(Intention.MOVE_TO, new Location(bot.getX() + Rnd.get(-150, 150), bot.getY() + Rnd.get(-150, 150), bot.getZ()));
				return;
			}
			else if (stuckCount >= 2)
			{
				if (Rnd.get(100) < 20) botSay(bot, "tp al spot...");
				bot.teleToLocation(zone.spotX + Rnd.get(-300, 300), zone.spotY + Rnd.get(-300, 300), zone.spotZ);
				bot.broadcastUserInfo(); 
				_stuckCounters.put(bot.getObjectId(), 0);
				return;
			}
			return; 
		}

		if (Rnd.get(100) < 1)
		{
			Player nearby = World.getInstance().getVisibleObjectsInRange(bot, Player.class, 1000).stream().filter(p -> !p.isDead() && !p.isGM() && !activePhantoms.contains(p)).findFirst().orElse(null);
			if (nearby != null) requestGemini(bot, nearby, "Dile algo casual y amigable a un jugador llamado " + nearby.getName() + " que pasó por tu lado.", false);
		}

		if (bot.isCastingNow() || bot.isAttackingNow() || bot.isMoving()) return;

		Player aggressor = World.getInstance().getVisibleObjectsInRange(bot, Player.class, 1500).stream().filter(p -> !p.isDead() && p.getTarget() == bot && p.getPvpFlag() > 0 && !p.isGM()).findFirst().orElse(null);
		if (aggressor != null) { bot.setTarget(aggressor); executeAttackPlan(bot, aggressor); return; }

		Player pkTarget = World.getInstance().getVisibleObjectsInRange(bot, Player.class, 1500).stream().filter(p -> !p.isDead() && p.getReputation() < 0 && !p.isGM() && !activePhantoms.contains(p)).findFirst().orElse(null);
		if (pkTarget != null) { bot.setTarget(pkTarget); executeAttackPlan(bot, pkTarget); return; }

		if (Rnd.get(100) < 5) 
		{
			Player innocent = World.getInstance().getVisibleObjectsInRange(bot, Player.class, 1000).stream().filter(p -> !p.isDead() && !p.isGM() && !activePhantoms.contains(p)).findFirst().orElse(null);
			if (innocent != null) { bot.updatePvPStatus(); bot.setTarget(innocent); executeAttackPlan(bot, innocent); return; }
		}

		List<Monster> validMobs = new ArrayList<>();
		for (Monster m : World.getInstance().getVisibleObjectsInRange(bot, Monster.class, 2500))
		{
			if (!m.isDead() && m.isAttackable())
			{
				int lvlDiff = m.getLevel() - bot.getLevel();
				if (lvlDiff >= -5 && lvlDiff <= 3)
				{
					boolean isTargetedByOtherPhantom = false;
					for (Player p : activePhantoms)
					{
						if (p != bot && p.getTarget() == m)
						{
							isTargetedByOtherPhantom = true;
							break;
						}
					}
					if (!isTargetedByOtherPhantom) validMobs.add(m);
				}
			}
		}

		if (!validMobs.isEmpty()) 
		{ 
			Monster target = validMobs.get(Rnd.get(validMobs.size()));
			bot.setTarget(target); 
			executeAttackPlan(bot, target); 
			return; 
		}

		int newX = bot.getX();
		int newY = bot.getY();
		
		Player closePhantom = World.getInstance().getVisibleObjectsInRange(bot, Player.class, 400).stream()
				.filter(p -> activePhantoms.contains(p) && p != bot).findFirst().orElse(null);
				
		if (closePhantom != null)
		{
			newX += (bot.getX() - closePhantom.getX() > 0 ? 1500 : -1500);
			newY += (bot.getY() - closePhantom.getY() > 0 ? 1500 : -1500);
		}
		else
		{
			newX += Rnd.get(-1500, 1500);
			newY += Rnd.get(-1500, 1500);
		}
		
		bot.getAI().setIntention(Intention.MOVE_TO, new Location(newX, newY, bot.getZ() + 100));
	}

	private void executeAttackPlan(Player bot, Creature target)
	{
		if (!bot.isChargedShot(ShotType.SOULSHOTS)) bot.chargeShot(ShotType.SOULSHOTS);
		if (!bot.isChargedShot(ShotType.BLESSED_SPIRITSHOTS)) bot.chargeShot(ShotType.BLESSED_SPIRITSHOTS);

		boolean isRecovering = _mpRecoveryState.getOrDefault(bot.getObjectId(), false);
		if (isRecovering && bot.getCurrentMp() >= (bot.getMaxMp() * 0.30)) { _mpRecoveryState.put(bot.getObjectId(), false); isRecovering = false; }

		if (!isRecovering)
		{
			List<Skill> offensiveSkills = new ArrayList<>();
			for (Skill sk : bot.getAllSkills()) { if (sk != null && sk.hasNegativeEffect() && !sk.isPassive()) offensiveSkills.add(sk); }

			if (!offensiveSkills.isEmpty())
			{
				Skill skillToCast = offensiveSkills.get(Rnd.get(offensiveSkills.size()));
				if (bot.getCurrentMp() >= skillToCast.getMpConsume())
				{
					bot.getAI().setIntention(Intention.CAST, skillToCast, target);
					return;
				}
				_mpRecoveryState.put(bot.getObjectId(), true);
			}
		}
		bot.getAI().setIntention(Intention.ATTACK, target);
	}

	@Override
	public boolean onCommand(String command, Player player, String target)
	{
		if (command.equalsIgnoreCase("pstart"))
		{
			if (!_isRunning) { _isRunning = true; loadAllPhantoms(); player.sendMessage("Sistema de Phantoms INICIADO."); }
			return true;
		}
		else if (command.equalsIgnoreCase("pstop"))
		{
			if (_isRunning)
			{
				_isRunning = false;
				for (Player p : activePhantoms) { if (p != null) p.deleteMe(); }
				activePhantoms.clear(); _mpRecoveryState.clear(); _phantomGearGrade.clear();
				_lastLocations.clear(); _stuckCounters.clear();
				player.sendMessage("Sistema de Phantoms DETENIDO.");
			}
			return true;
		}
		else if (command.equalsIgnoreCase("pload"))
		{
			loadXML(); player.sendMessage("XML de Phantoms recargado (" + PHANTOM_IDS.size() + " bots encontrados).");
			if (_isRunning) loadAllPhantoms();
			return true;
		}
		else if (command.equalsIgnoreCase("pm"))
		{
			if (target == null || target.isEmpty()) return true;
			String[] parts = target.split(" ", 2);
			if (parts.length < 2) return true;
			
			String phantomName = parts[0];
			String message = parts[1];

			Player targetPhantom = activePhantoms.stream().filter(p -> p.getName().equalsIgnoreCase(phantomName)).findFirst().orElse(null);

			if (targetPhantom != null)
			{
				player.sendPacket(new CreatureSay(player, ChatType.WHISPER, "->" + targetPhantom.getName(), message));
				requestGemini(targetPhantom, player, message, true);
			}
			else player.sendMessage("El Phantom '" + phantomName + "' no esta farmeando actualmente.");
			return true;
		}
		else if (command.equalsIgnoreCase("pmenu"))
		{
			if (!player.isGM()) return false;
			
			StringBuilder html = new StringBuilder();
			html.append("<html><title>Phantom Manager</title><body>");
			html.append("<center><font color=\"LEVEL\">Panel de Control de Phantoms</font></center><br>");
			html.append("<table width=280>");
			
			if (activePhantoms.isEmpty()) {
				html.append("<tr><td><center>El sistema esta apagado o no hay bots.</center></td></tr>");
			} else {
				for (Player p : activePhantoms)
				{
					html.append("<tr>");
					html.append("<td width=100>").append(p.getName()).append(" (L").append(p.getLevel()).append(")</td>");
					html.append("<td width=50><button value=\"Ir\" action=\"bypass -h voice_pgo ").append(p.getName()).append("\" width=45 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
					html.append("<td width=50><button value=\"Traer\" action=\"bypass -h voice_pbring ").append(p.getName()).append("\" width=45 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
					html.append("<td width=50><button value=\"Matar\" action=\"bypass -h voice_pkill ").append(p.getName()).append("\" width=45 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
					html.append("</tr>");
				}
			}
			html.append("</table><br><center>");
			html.append("Si los botones fallan, usa los comandos:<br>");
			html.append("<font color=\"LEVEL\">.pgo Nombre</font> | <font color=\"LEVEL\">.pbring Nombre</font>");
			html.append("</center></body></html>");
			
			NpcHtmlMessage msg = new NpcHtmlMessage();
			msg.setHtml(html.toString());
			player.sendPacket(msg);
			return true;
		}
		else if (command.startsWith("pgo"))
		{
			if (!player.isGM()) return false;
			String targetName = command.replace("pgo", "").trim();
			Player p = activePhantoms.stream().filter(bot -> bot.getName().equalsIgnoreCase(targetName)).findFirst().orElse(null);
			if (p != null) player.teleToLocation(p.getLocation());
			else player.sendMessage("Phantom no encontrado.");
			return true;
		}
		else if (command.startsWith("pbring"))
		{
			if (!player.isGM()) return false;
			String targetName = command.replace("pbring", "").trim();
			Player p = activePhantoms.stream().filter(bot -> bot.getName().equalsIgnoreCase(targetName)).findFirst().orElse(null);
			if (p != null) { p.teleToLocation(player.getLocation()); p.broadcastUserInfo(); }
			else player.sendMessage("Phantom no encontrado.");
			return true;
		}
		else if (command.startsWith("pkill"))
		{
			if (!player.isGM()) return false;
			String targetName = command.replace("pkill", "").trim();
			Player p = activePhantoms.stream().filter(bot -> bot.getName().equalsIgnoreCase(targetName)).findFirst().orElse(null);
			if (p != null && !p.isDead()) { p.doDie(player); player.sendMessage("Has matado a " + targetName); }
			else player.sendMessage("Phantom no encontrado o ya esta muerto.");
			return true;
		}
		return false;
	}

	@Override
	public String[] getCommandList() { return VOICED_COMMANDS; }

	public static void main(String[] args) { new PhantomManager(); }
}
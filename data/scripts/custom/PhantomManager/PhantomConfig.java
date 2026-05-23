package custom.PhantomManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class PhantomConfig
{
	public static final List<Integer> PHANTOM_IDS = new ArrayList<>();
	public static final List<FarmZone> ROUTES = new ArrayList<>();
	
	public static final int[][] FIGHTER_GEAR =
	{
		{
			19,
			23,
			42
		},
		{
			28,
			2386,
			2382
		},
		{
			122,
			338,
			356
		},
		{
			74,
			2381,
			2380
		},
		{
			80,
			2376
		},
		{
			6580,
			6373,
			6374
		}
	};
	public static final int[][] MAGE_GEAR =
	{
		{
			17,
			46,
			47
		},
		{
			1120,
			439,
			471
		},
		{
			1224,
			438,
			470
		},
		{
			1229,
			2406
		},
		{
			1230,
			2407
		},
		{
			6608,
			2409
		}
	};
	public static final int[] FIGHTER_SHOTS =
	{
		1463,
		1464,
		1465,
		1466,
		1467,
		1462
	};
	public static final int[] MAGE_SHOTS =
	{
		3947,
		3948,
		3949,
		3950,
		3951,
		3952
	};
	
	public static class FarmZone
	{
		public int minLvl, maxLvl, spotX, spotY, spotZ, townX, townY, townZ;
		
		public FarmZone(int min, int max, int sx, int sy, int sz, int tx, int ty, int tz)
		{
			this.minLvl = min;
			this.maxLvl = max;
			this.spotX = sx;
			this.spotY = sy;
			this.spotZ = sz;
			this.townX = tx;
			this.townY = ty;
			this.townZ = tz;
		}
	}
	
	public static void init()
	{
		ROUTES.clear();
		ROUTES.add(new FarmZone(1, 15, -75291, 251836, -3336, -84318, 244579, -3730));
		ROUTES.add(new FarmZone(15, 25, -43295, 118875, -2600, -14225, 123540, -3121));
		ROUTES.add(new FarmZone(25, 35, 33924, 134785, -2500, 15670, 142983, -2705));
		ROUTES.add(new FarmZone(35, 45, 47214, 147572, -2000, 15670, 142983, -2705));
		ROUTES.add(new FarmZone(45, 55, 138584, 9639, -3500, 146142, 26715, -2200));
		ROUTES.add(new FarmZone(55, 65, 109240, 40968, -4000, 117110, 76883, -2695));
		ROUTES.add(new FarmZone(65, 75, 140406, 12433, -3400, 146142, 26715, -2200));
		ROUTES.add(new FarmZone(75, 80, 172310, 52740, -4800, 146142, 26715, -2200));
		ROUTES.add(new FarmZone(80, 85, 110000, 118000, -2500, 83400, 147943, -3404));
		ROUTES.add(new FarmZone(85, 100, -55680, 136162, -2200, -13973, 122208, -3116));
		loadXML();
		PhantomHuntingSpots.load();
	}
	
	public static void loadXML()
	{
		PHANTOM_IDS.clear();
		try
		{
			File file = new File("config/Custom/PhantomPlayers.xml");
			if (!file.exists())
			{
				System.out.println(">>> [PHANTOM SYSTEM] ERROR: config/Custom/PhantomPlayers.xml NO ENCONTRADO.");
				return;
			}
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			NodeList list = doc.getElementsByTagName("phantom");
			for (int i = 0; i < list.getLength(); i++)
			{
				PHANTOM_IDS.add(Integer.parseInt(list.item(i).getAttributes().getNamedItem("charId").getNodeValue()));
			}
			System.out.println(">>> [PHANTOM SYSTEM] XML cargado exitosamente. " + PHANTOM_IDS.size() + " IDs.");
		}
		catch (Exception e)
		{
			System.out.println(">>> [PHANTOM SYSTEM] ERROR LEYENDO XML: " + e.getMessage());
		}
	}
	
	public static FarmZone getIdealZone(int level)
	{
		for (FarmZone zone : ROUTES)
		{
			if ((level >= zone.minLvl) && (level < zone.maxLvl))
			{
				return zone;
			}
		}
		return ROUTES.get(ROUTES.size() - 1);
	}
}

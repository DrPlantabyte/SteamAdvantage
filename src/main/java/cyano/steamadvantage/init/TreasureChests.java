package cyano.steamadvantage.init;

import cyano.steamadvantage.SteamAdvantage;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class TreasureChests {

	private static boolean initDone = false;
	public static void init(Path configFolder){
		if(initDone)return;

		Path chestFolder = configFolder.resolve(Paths.get("additional-loot-tables", SteamAdvantage.MODID,"chests"));
		writeLootFile(chestFolder.resolve("abandoned_mineshaft.json"), LOOT_POOL);
		writeLootFile(chestFolder.resolve("simple_dungeon.json"), LOOT_POOL);
		writeLootFile(chestFolder.resolve("village_blacksmith.json"), LOOT_POOL);
		writeLootFile(chestFolder.resolve("stronghold_corridor.json"), LOOT_POOL);
		writeLootFile(chestFolder.resolve("stronghold_crossing.json"), LOOT_POOL);
		writeLootFile(chestFolder.resolve("nether_bridge.json"), LOOT_POOL);

		initDone = true;
	}

	private static void writeLootFile(Path file, String content){
		try {
			Files.createDirectories(file.getParent());
			Files.write(file, Arrays.asList(content), Charset.forName("UTF-8"));
		} catch (IOException e) {
			FMLLog.log(Level.ERROR,e,"Error writing additional-loot-table files");
		}
	}


	private static final String LOOT_POOL = "{\n" +
			"    \"pools\": [\n" +
			"        {\n" +
			"            \"__comment\":\"25% chance of a Steam Advantage item\",\n" +
			"            \"rolls\": 1,\n" +
			"            \"entries\": [\n" +
			"                {\n" +
			"                    \"type\": \"empty\",\n" +
			"                    \"weight\": 135\n" +
			"                },\n" +
			"                {\n" +
			"                    \"type\": \"item\",\n" +
			"                    \"name\": \"steamadvantage:steam_governor\",\n" +
			"                    \"weight\": 50,\n" +
			"                    \"functions\": [\n" +
			"                        {\n" +
			"                            \"function\": \"set_count\",\n" +
			"                            \"count\": {\n" +
			"                                \"min\": 1,\n" +
			"                                \"max\": 6\n" +
			"                            }\n" +
			"                        }\n" +
			"                    ]\n" +
			"                },\n" +
			"                {\n" +
			"                    \"type\": \"item\",\n" +
			"                    \"name\": \"steamadvantage:steam_pipe\",\n" +
			"                    \"weight\": 25,\n" +
			"                    \"functions\": [\n" +
			"                        {\n" +
			"                            \"function\": \"set_count\",\n" +
			"                            \"count\": {\n" +
			"                                \"min\": 3,\n" +
			"                                \"max\": 6\n" +
			"                            }\n" +
			"                        }\n" +
			"                    ]\n" +
			"                },\n" +
			"                {\n" +
			"                    \"type\": \"item\",\n" +
			"                    \"name\": \"steamadvantage:blackpowder_cartridge\",\n" +
			"                    \"weight\": 50,\n" +
			"                    \"functions\": [\n" +
			"                        {\n" +
			"                            \"function\": \"set_count\",\n" +
			"                            \"count\": {\n" +
			"                                \"min\": 5,\n" +
			"                                \"max\": 15\n" +
			"                            }\n" +
			"                        }\n" +
			"                    ]\n" +
			"                },\n" +
			"                {\n" +
			"                    \"type\": \"item\",\n" +
			"                    \"name\": \"steamadvantage:musket\",\n" +
			"                    \"weight\": 18\n" +
			"                },\n" +
			"                {\n" +
			"                    \"type\": \"item\",\n" +
			"                    \"name\": \"steamadvantage:musket\",\n" +
			"                    \"weight\": 2,\n" +
			"                    \"functions\": [\n" +
			"                        {\n" +
			"                            \"function\": \"minecraft:enchant_with_levels\",\n" +
			"                            \"levels\": 10,\n" +
			"                            \"treasure\": true\n" +
			"                        }\n" +
			"                    ]\n" +
			"                }\n" +
			"            ]\n" +
			"        }\n" +
			"    ]\n" +
			"}\n" +
			"\n";
}

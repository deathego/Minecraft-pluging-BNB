package com.questengine;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import com.questengine.MinecraftQuestPlugin;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class QuestManager {
    private static final Map<UUID, BossBar> activeBossBars = new ConcurrentHashMap<>();
    private static final Map<UUID, Quest> activeQuests = new HashMap<>();
    private static final Map<UUID, List<ObjectiveProgress>> progressMap = new HashMap<>();
    private static File file;
    private static YamlConfiguration data;
    private static File walletFile;
    private static YamlConfiguration walletData;
    // Setup file and load existing data
    public static void setup(File dataFolder) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        // quests.yml
        file = new File(dataFolder, "quests.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().warning("⚠ Could not create quests.yml");
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
        // wallets.yml
        walletFile = new File(dataFolder, "wallets.yml");
        if (!walletFile.exists()) {
            try {
                walletFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().warning("⚠ Could not create wallets.yml");
            }
        }
        walletData = YamlConfiguration.loadConfiguration(walletFile);
        loadFromFile(); // load quests only
    }
    // Assign quest to player
    public static void setQuest(UUID playerId, Quest quest) {
        BossBar oldBar = activeBossBars.remove(playerId);
        if (oldBar != null) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) oldBar.removeAll();
        }
        activeQuests.put(playerId, quest);
        initializeProgress(playerId, quest.getObjectives());
        saveToFile();
    }
    // Retrieve active quest
    public static Quest getQuest(UUID playerId) {
        return activeQuests.get(playerId);
    }
    public static String getQuestTitle(UUID playerId) {
        Quest quest = activeQuests.get(playerId);
        return quest != null ? quest.getTitle() : null;
    }
    public static boolean hasActiveQuest(UUID playerId) {
        return activeQuests.containsKey(playerId);
    }
    // Complete quest and mint SBT
    public static void completeQuest(UUID playerId) {
        BossBar bossBar = activeBossBars.remove(playerId);
        if (bossBar != null) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) bossBar.removeAll();
        }
        Quest quest = activeQuests.get(playerId);
        activeQuests.remove(playerId);
        progressMap.remove(playerId);
        data.set(playerId.toString(), null);
        saveToFile();
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            Bukkit.getScheduler().runTaskAsynchronously(
                MinecraftQuestPlugin.getInstance()
,
                () -> {
                    try {
                        URL url = new URL("http://localhost:3000/api/mint");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setDoOutput(true);

                        String jsonInput = String.format(
  "{\"playerWallet\": \"%s\", \"tokenURI\": \"%s\"}",
  getWalletAddress(player.getUniqueId()),
  "ipfs://QmYXDrLGfHggb7eNyozYV2C2eZfgtHbNuDdEqdQh5StXaj"
);
                        try (OutputStream os = conn.getOutputStream()) {
                            byte[] input = jsonInput.getBytes("utf-8");
                            os.write(input, 0, input.length);
                        }

                        int code = conn.getResponseCode();
                        Bukkit.getLogger().info("🧠 SBT Minting HTTP Status: " + code);
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("⚠ Failed to mint SBT: " + e.getMessage());
                    }
                }
            );
        }
    }
    // Initialize progress
    public static void initializeProgress(UUID playerId, List<Objective> objectives) {
        List<ObjectiveProgress> progressList = new ArrayList<>();
        for (Objective obj : objectives) {
            progressList.add(new ObjectiveProgress(obj.getType(), obj.getItem(), obj.getAmount()));
        }
        progressMap.put(playerId, progressList);
    }
    public static List<ObjectiveProgress> getProgress(UUID playerId) {
        return progressMap.getOrDefault(playerId, new ArrayList<>());
    }
    public static void incrementProgress(UUID playerId, String type, String item) {
        List<ObjectiveProgress> list = progressMap.get(playerId);
        if (list == null) return;

        for (ObjectiveProgress obj : list) {
            if (obj.getType().equalsIgnoreCase(type) && obj.getItem().equalsIgnoreCase(item)) {
                obj.increment();
                Bukkit.getLogger().info("✅ Progress incremented for " + playerId + ": " + type + " " + item);
                break;
            }
        }
        saveToFile();
    }
    public static boolean isQuestComplete(UUID playerId) {
        List<ObjectiveProgress> list = progressMap.get(playerId);
        if (list == null) return false;

        for (ObjectiveProgress obj : list) {
            Bukkit.getLogger().info("🔍 Checking objective: " + obj.getItem() + " progress: " + obj.getCurrentAmount() + "/" + obj.getTargetAmount());
            if (!obj.isComplete()) return false;
        }
        return true;
    }
    private static void saveToFile() {
        for (UUID uuid : activeQuests.keySet()) {
            Quest quest = activeQuests.get(uuid);
            String base = uuid.toString();
            data.set(base + ".title", quest.getTitle());
            data.set(base + ".description", quest.getDescription());

            List<Map<String, Object>> serializedObjectives = new ArrayList<>();
            for (Objective obj : quest.getObjectives()) {
                Map<String, Object> map = new HashMap<>();
                map.put("type", obj.getType());
                map.put("item", obj.getItem());
                map.put("amount", obj.getAmount());
                serializedObjectives.add(map);
            }
            data.set(base + ".objectives", serializedObjectives);
            List<ObjectiveProgress> progressList = progressMap.get(uuid);
            List<Map<String, Object>> serializedProgress = new ArrayList<>();
            if (progressList != null) {
                for (ObjectiveProgress prog : progressList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", prog.getType());
                    map.put("item", prog.getItem());
                    map.put("target", prog.getTargetAmount());
                    map.put("current", prog.getCurrentAmount());
                    serializedProgress.add(map);
                }
            }
            data.set(base + ".progress", serializedProgress);
        }
        try {
            data.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().warning("⚠ Could not save quests.yml");
        }
    }
    private static void loadFromFile() {
        activeQuests.clear();
        progressMap.clear();
        for (String uuidString : data.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                String title = data.getString(uuidString + ".title");
                String description = data.getString(uuidString + ".description");

                List<Objective> objectives = new ArrayList<>();
                List<?> objList = data.getList(uuidString + ".objectives");
                if (objList != null) {
                    for (Object raw : objList) {
                        if (raw instanceof Map) {
                            Map<?, ?> map = (Map<?, ?>) raw;
                            String type = (String) map.get("type");
                            String item = (String) map.get("item");
                            int amount = ((Number) map.get("amount")).intValue();
                            objectives.add(new Objective(type, item, amount));
                        }
                    }
                }
                activeQuests.put(uuid, new Quest(title, description, objectives));
                List<ObjectiveProgress> progressList = new ArrayList<>();
                List<?> progList = data.getList(uuidString + ".progress");
                if (progList != null) {
                    for (Object raw : progList) {
                        if (raw instanceof Map) {
                            Map<?, ?> map = (Map<?, ?>) raw;
                            String type = (String) map.get("type");
                            String item = (String) map.get("item");
                            int target = ((Number) map.get("target")).intValue();
                            int current = ((Number) map.get("current")).intValue();
                            ObjectiveProgress prog = new ObjectiveProgress(type, item, target);
                            prog.setCurrentAmount(current);
                            progressList.add(prog);
                        }
                    }
                }
                progressMap.put(uuid, progressList);
            } catch (Exception e) {
                Bukkit.getLogger().warning("⚠ Failed to load quest for UUID " + uuidString);
            }
        }
    }
    public static String getWalletAddress(UUID uuid) {
        return walletData.getString(uuid.toString(), "0x0000000000000000000000000000000000000000");
    }
    public static void setWalletAddress(UUID uuid, String address) {
        walletData.set(uuid.toString(), address);
        try {
            walletData.save(walletFile);
        } catch (IOException e) {
            Bukkit.getLogger().warning("⚠ Could not save wallet address for " + uuid);
        }
    }
}

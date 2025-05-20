package com.questengine;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class QuestManager {
    private static final Map<UUID, Quest> activeQuests = new HashMap<>();
    private static File file;
    private static YamlConfiguration data;

    public static void setup(File dataFolder) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs(); // Ensure plugin folder exists
        }

        file = new File(dataFolder, "quests.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().warning("⚠ Could not create quests.yml");
            }
        }

        data = YamlConfiguration.loadConfiguration(file);
        loadFromFile();
    }

    public static void setQuest(UUID playerId, Quest quest) {
        activeQuests.put(playerId, quest);
        saveToFile();
    }

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

    public static void completeQuest(UUID playerId) {
        activeQuests.remove(playerId);
        data.set(playerId.toString(), null);
        saveToFile();
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
        }

        try {
            data.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().warning("⚠ Could not save quests.yml");
        }
    }

    private static void loadFromFile() {
        activeQuests.clear();

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
            } catch (Exception e) {
                Bukkit.getLogger().warning("⚠ Failed to load quest for UUID " + uuidString);
            }
        }
    }
}

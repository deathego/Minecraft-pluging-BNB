package com.questengine;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class QuestManager {
    private static final Map<UUID, Quest> activeQuests = new HashMap<>();
    private static final Map<UUID, List<ObjectiveProgress>> progressMap = new HashMap<>();
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
        initializeProgress(playerId, quest.getObjectives());
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
        progressMap.remove(playerId);
        data.set(playerId.toString(), null);
        saveToFile();
    }

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
}

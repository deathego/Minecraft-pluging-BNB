package com.questengine;

import java.util.HashMap;
import java.util.UUID;

public class QuestManager {
    private static final HashMap<UUID, String> activeQuests = new HashMap<>();

    public static boolean hasActiveQuest(UUID playerId) {
        return activeQuests.containsKey(playerId);
    }

    public static void setActiveQuest(UUID playerId, String questTitle) {
        activeQuests.put(playerId, questTitle);
    }

    public static void completeQuest(UUID playerId) {
        activeQuests.remove(playerId);
    }

    public static String getQuestTitle(UUID playerId) {
        return activeQuests.get(playerId);
    }
}

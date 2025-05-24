package com.questengine;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class BossBarManager {
    private static final HashMap<UUID, Integer> taskIds = new HashMap<>();
    private static final HashMap<UUID, BossBar> playerBars = new HashMap<>();
    private static JavaPlugin plugin;

    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
    }

    public static void showQuestTimer(Player player, int totalSeconds) {
    UUID uuid = player.getUniqueId();

    
    removeBar(player);

    BossBar bar = Bukkit.createBossBar("⚔ " + player.getName() + " - Time left: 15:00", BarColor.BLUE, BarStyle.SEGMENTED_12);
    bar.setProgress(1.0);
    bar.addPlayer(player);
    playerBars.put(uuid, bar);

    int taskId = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
        int secondsLeft = totalSeconds;

        @Override
        public void run() {
            if (!bar.getPlayers().contains(player)) {
                bar.removeAll();
                playerBars.remove(uuid);
                cancelTask(uuid);
                return;
            }

            if (!QuestManager.hasActiveQuest(uuid)) {
                bar.setTitle("⚔ " + player.getName() + " - No active quest");
                bar.setColor(BarColor.GREEN);
                bar.setProgress(0);
                cancelTask(uuid);
                return;
            }

            if (secondsLeft <= 0) {
                QuestManager.completeQuest(uuid);
                bar.setTitle("⚔ " + player.getName() + " - No active quest");
                bar.setColor(BarColor.RED);
                bar.setProgress(0);
                cancelTask(uuid);
                return;
            }

            int minutes = secondsLeft / 60;
            int seconds = secondsLeft % 60;
            String timeStr = String.format("%02d:%02d", minutes, seconds);
            bar.setTitle("⚔ " + player.getName() + " - Time left: " + timeStr);
            bar.setProgress(secondsLeft / (double) totalSeconds);
            secondsLeft--;
        }
    }, 0L, 20L).getTaskId();

    taskIds.put(uuid, taskId);
}


    public static void removeBar(Player player) {
    UUID uuid = player.getUniqueId();
    BossBar bar = playerBars.remove(uuid);
    if (bar != null) bar.removeAll();
    cancelTask(uuid);
}

    private static void cancelTask(UUID uuid) {
    Integer taskId = taskIds.remove(uuid);
    if (taskId != null) {
        Bukkit.getScheduler().cancelTask(taskId);
    }
}


    public static void showNoActiveQuest(Player player) {
        BossBar bar = Bukkit.createBossBar("⚔ " + player.getName() + " - No active quest", BarColor.GREEN, BarStyle.SOLID);
        bar.setProgress(0);
        bar.addPlayer(player);
        playerBars.put(player.getUniqueId(), bar);
    }
}

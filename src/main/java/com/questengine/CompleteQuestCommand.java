package com.questengine;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;

public class CompleteQuestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        
        if (!QuestManager.hasActiveQuest(player.getUniqueId())) {
            player.sendMessage("§cYou don't have any active quest to complete.");
            return true;
        }

        String questTitle = QuestManager.getQuestTitle(player.getUniqueId());
        String completedAt = Instant.now().toString();

        String json = String.format(
                "{\"playerName\":\"%s\", \"questTitle\":\"%s\", \"completedAt\":\"%s\"}",
                player.getName(), questTitle, completedAt
        );

        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getProvidingPlugin(getClass()), () -> {
            try {
                Request.post("http://localhost:3000/submit-quest")
                        .bodyString(json, ContentType.APPLICATION_JSON)
                        .execute()
                        .discardContent();

                
                QuestManager.completeQuest(player.getUniqueId());

                player.sendMessage("§aQuest marked as completed and sent to server!");
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage("§cFailed to submit quest.");
            }
        });

        return true;
    }
}

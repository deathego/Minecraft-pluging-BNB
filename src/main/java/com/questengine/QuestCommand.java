package com.questengine;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public class QuestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // 🔒 Check if the player already has an active quest
        if (QuestManager.hasActiveQuest(player.getUniqueId())) {
            player.sendMessage("§cYou already have an active quest! Complete it before taking a new one.");
            return true;
        }

        player.sendMessage("§eGenerating a new quest...");
        Logger logger = Bukkit.getLogger();

        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getProvidingPlugin(getClass()), () -> {
            try {
                String response = Request.post("http://localhost:3000/generate-quest")
                        .bodyString("{}", ContentType.APPLICATION_JSON)
                        .execute()
                        .returnContent()
                        .asString();

                logger.info("Raw response: " + response);

                JSONParser parser = new JSONParser();
                JSONObject quest = (JSONObject) parser.parse(response);

                String title = (String) quest.get("title");
                String description = (String) quest.get("description");

                StringBuilder message = new StringBuilder();
                message.append("§aQuest: §f").append(title).append("\n");
                message.append("§7").append(description).append("\n§bObjectives:\n");

                Object objectiveObj = quest.get("objective");
                if (objectiveObj instanceof JSONArray) {
                    JSONArray objectives = (JSONArray) objectiveObj;
                    for (Object obj : objectives) {
                        JSONObject objective = (JSONObject) obj;
                        message.append(" - ").append(objective.get("type")).append(" ")
                                .append(objective.get("amount")).append(" ")
                                .append(objective.get("item")).append("\n");
                    }
                } else {
                    logger.warning("Objective is not an array: " + objectiveObj);
                }

                Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(getClass()), () -> {
                    player.sendMessage(message.toString());
                    // ✅ Store the quest as active for this player
                    QuestManager.setActiveQuest(player.getUniqueId(), title);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(getClass()), () ->
                        player.sendMessage("§cFailed to fetch quest from backend.")
                );
            }
        });

        return true;
    }
}

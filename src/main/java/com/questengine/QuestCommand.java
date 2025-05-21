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

import java.util.List;
import java.util.logging.Logger;

public class QuestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Check if player already has an active quest
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
                JSONObject questJson = (JSONObject) parser.parse(response);

                String title = (String) questJson.get("title");
                String description = (String) questJson.get("description");

                Object objectiveObj = questJson.get("objective");
                if (!(objectiveObj instanceof JSONArray)) {
                    logger.warning("Objective is not an array: " + objectiveObj);
                    Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(getClass()), () ->
                            player.sendMessage("§cFailed to parse quest objectives.")
                    );
                    return;
                }

                JSONArray objectiveArray = (JSONArray) objectiveObj;
                List<Objective> objectives = new java.util.ArrayList<>();

                StringBuilder message = new StringBuilder();
                message.append("§aQuest: §f").append(title).append("\n");
                message.append("§7").append(description).append("\n§bObjectives:\n");

                for (Object obj : objectiveArray) {
                    JSONObject o = (JSONObject) obj;
                    String type = (String) o.get("type");
                    String item = (String) o.get("item");
                    int amount = ((Long) o.get("amount")).intValue();

                    objectives.add(new Objective(type, item, amount));
                    message.append(" - ").append(type).append(" ").append(amount).append(" ").append(item).append("\n");
                }

                Quest fullQuest = new Quest(title, description, objectives);

Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(getClass()), () -> {
    player.sendMessage(message.toString());

    // ✅ Save the full quest object
    QuestManager.setQuest(player.getUniqueId(), fullQuest);

    // ✅ Initialize objective progress
    QuestManager.initializeProgress(player.getUniqueId(), objectives);
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

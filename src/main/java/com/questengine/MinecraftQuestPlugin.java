package com.questengine;

import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftQuestPlugin extends JavaPlugin {

    public void onEnable() {
    QuestManager.setup(getDataFolder());

    this.getCommand("quest").setExecutor(new QuestCommand());
    this.getCommand("completequest").setExecutor(new CompleteQuestCommand());
    getLogger().info("✅ Minecraft Quest Plugin Enabled!");
}

    @Override
    public void onDisable() {
        getLogger().info("🛑 Minecraft Quest Plugin Disabled!");
    }
}

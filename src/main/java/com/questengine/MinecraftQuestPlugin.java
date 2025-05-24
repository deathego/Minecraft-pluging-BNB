package com.questengine;
import org.bukkit.plugin.java.JavaPlugin;
public class MinecraftQuestPlugin extends JavaPlugin {
    private static MinecraftQuestPlugin instance;
    @Override
    public void onEnable() {
        instance = this;  

        
        QuestManager.setup(getDataFolder());
        
        BossBarManager.init(this);
        
        this.getCommand("quest").setExecutor(new QuestCommand());
        this.getCommand("completequest").setExecutor(new CompleteQuestCommand());
        this.getCommand("linkwallet").setExecutor(new LinkWalletCommand());
       
        getServer().getPluginManager().registerEvents(new QuestEventListener(), this);
        getLogger().info("✅ Minecraft Quest Plugin Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("🛑 Minecraft Quest Plugin Disabled!");
    }
    
    public static MinecraftQuestPlugin getInstance() {
        return instance;
    }
}

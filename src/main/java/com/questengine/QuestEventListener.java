package com.questengine;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import java.util.UUID;
public class QuestEventListener implements Listener {
    @EventHandler
public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();

    if (QuestManager.hasActiveQuest(playerId)) {
        String questTitle = QuestManager.getQuestTitle(playerId);
        if (questTitle != null) {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "📘 Welcome back! Your active quest: " + ChatColor.BOLD + questTitle);
        }
    }
}
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) return;

        Player player = event.getEntity().getKiller();
        UUID playerId = player.getUniqueId();
        if (!QuestManager.hasActiveQuest(playerId)) return;
        String entityName = event.getEntityType().name().toLowerCase();
        QuestManager.incrementProgress(playerId, "defeat", entityName);
        player.sendMessage(ChatColor.GREEN + "⚔ You defeated a " + entityName);
        checkAndComplete(player);
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!QuestManager.hasActiveQuest(playerId)) return;

        String blockType = event.getBlock().getType().name().toLowerCase();
        QuestManager.incrementProgress(playerId, "break", blockType);

        player.sendMessage(ChatColor.YELLOW + "🪓 You broke a " + blockType);
        checkAndComplete(player);
    }
    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!QuestManager.hasActiveQuest(playerId)) return;

        ItemStack item = event.getItem().getItemStack();
        String itemType = item.getType().name().toLowerCase();

        QuestManager.incrementProgress(playerId, "collect", itemType);
        player.sendMessage(ChatColor.AQUA + "📦 You collected a " + itemType);
        checkAndComplete(player);
    }
    private void checkAndComplete(Player player) {
        UUID playerId = player.getUniqueId();
        if (QuestManager.isQuestComplete(playerId)) {
            player.sendMessage(ChatColor.GOLD + "🎉 Quest complete! Great job.");
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            player.spawnParticle(Particle.HEART, player.getLocation(), 30);
            QuestManager.completeQuest(playerId);
        }
    }
}

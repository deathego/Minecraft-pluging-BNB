package com.questengine;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LinkWalletCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length != 1 || !args[0].startsWith("0x") || args[0].length() != 42) {
            sender.sendMessage("§cUsage: /linkwallet <your_wallet_address>");
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        QuestManager.setWalletAddress(uuid, args[0]);
        player.sendMessage("§a✅ Wallet address linked successfully!");
        System.out.println("Linked wallet for player: " + args[0]);
        return true;
    }
}

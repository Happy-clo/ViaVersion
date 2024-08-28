/*
 * This file is part of ViaVersion - https:
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http:
 */
package com.viaversion.viaversion.platform;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.util.Pair;
import com.viaversion.viaversion.util.ReflectionUtil;
import com.viaversion.viaversion.util.SynchronizedListWrapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;
public abstract class LegacyViaInjector implements ViaInjector {
    protected final List<ChannelFuture> injectedFutures = new ArrayList<>();
    protected final List<Pair<Field, Object>> injectedLists = new ArrayList<>();
    @Override
    public void inject() throws ReflectiveOperationException {
        Object connection = getServerConnection();
        if (connection == null) {
            throw new RuntimeException("Failed to find the core component 'ServerConnection'");
        }
        for (Field field : connection.getClass().getDeclaredFields()) {
            if (!List.class.isAssignableFrom(field.getType()) || !field.getGenericType().getTypeName().contains(ChannelFuture.class.getName())) {
                continue;
            }
            field.setAccessible(true);
            List<ChannelFuture> list = (List<ChannelFuture>) field.get(connection);
            List<ChannelFuture> wrappedList = new SynchronizedListWrapper(list, o -> {
                try {
                    injectChannelFuture((ChannelFuture) o);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            });
            synchronized (list) {
                for (ChannelFuture future : list) {
                    injectChannelFuture(future);
                }
                field.set(connection, wrappedList);
            }
            injectedLists.add(new Pair<>(field, connection));
        }
    }
    private void injectChannelFuture(ChannelFuture future) throws ReflectiveOperationException {
        List<String> names = future.channel().pipeline().names();
        ChannelHandler bootstrapAcceptor = null;
        for (String name : names) {
            ChannelHandler handler = future.channel().pipeline().get(name);
            try {
                ReflectionUtil.get(handler, "childHandler", ChannelInitializer.class);
                bootstrapAcceptor = handler;
                break;
            } catch (ReflectiveOperationException ignored) {
            }
        }
        if (bootstrapAcceptor == null) {
            bootstrapAcceptor = future.channel().pipeline().first();
        }
        try {
            ChannelInitializer<Channel> oldInitializer = ReflectionUtil.get(bootstrapAcceptor, "childHandler", ChannelInitializer.class);
            ReflectionUtil.set(bootstrapAcceptor, "childHandler", createChannelInitializer(oldInitializer));
            injectedFutures.add(future);
        } catch (NoSuchFieldException ignored) {
            blame(bootstrapAcceptor);
        }
    }
    @Override
    public void uninject() throws ReflectiveOperationException {
        for (ChannelFuture future : injectedFutures) {
            ChannelPipeline pipeline = future.channel().pipeline();
            ChannelHandler bootstrapAcceptor = pipeline.first();
            if (bootstrapAcceptor == null) {
                Via.getPlatform().getLogger().info("Empty pipeline, nothing to uninject");
                continue;
            }
            for (String name : pipeline.names()) {
                ChannelHandler handler = pipeline.get(name);
                if (handler == null) {
                    Via.getPlatform().getLogger().warning("Could not get handler " + name);
                    continue;
                }
                try {
                    if (ReflectionUtil.get(handler, "childHandler", ChannelInitializer.class) instanceof WrappedChannelInitializer) {
                        bootstrapAcceptor = handler;
                        break;
                    }
                } catch (ReflectiveOperationException ignored) {
                }
            }
            try {
                ChannelInitializer<Channel> initializer = ReflectionUtil.get(bootstrapAcceptor, "childHandler", ChannelInitializer.class);
                if (initializer instanceof WrappedChannelInitializer wrappedChannelInitializer) {
                    ReflectionUtil.set(bootstrapAcceptor, "childHandler", wrappedChannelInitializer.original());
                }
            } catch (Exception e) {
                Via.getPlatform().getLogger().log(Level.SEVERE, "Failed to remove injection handler, reload won't work with connections, please reboot!", e);
            }
        }
        injectedFutures.clear();
        for (Pair<Field, Object> pair : injectedLists) {
            try {
                Field field = pair.key();
                Object o = field.get(pair.value());
                if (o instanceof SynchronizedListWrapper) {
                    List<ChannelFuture> originalList = ((SynchronizedListWrapper) o).originalList();
                    synchronized (originalList) {
                        field.set(pair.value(), originalList);
                    }
                }
            } catch (ReflectiveOperationException e) {
                Via.getPlatform().getLogger().severe("Failed to remove injection, reload won't work with connections, please reboot!");
            }
        }
        injectedLists.clear();
    }
    @Override
    public boolean lateProtocolVersionSetting() {
        return true;
    }
    @Override
    public JsonObject getDump() {
        JsonObject data = new JsonObject();
        JsonArray injectedChannelInitializers = new JsonArray();
        data.add("injectedChannelInitializers", injectedChannelInitializers);
        for (ChannelFuture future : injectedFutures) {
            JsonObject futureInfo = new JsonObject();
            injectedChannelInitializers.add(futureInfo);
            futureInfo.addProperty("futureClass", future.getClass().getName());
            futureInfo.addProperty("channelClass", future.channel().getClass().getName());
            JsonArray pipeline = new JsonArray();
            futureInfo.add("pipeline", pipeline);
            for (String pipeName : future.channel().pipeline().names()) {
                JsonObject handlerInfo = new JsonObject();
                pipeline.add(handlerInfo);
                handlerInfo.addProperty("name", pipeName);
                ChannelHandler channelHandler = future.channel().pipeline().get(pipeName);
                if (channelHandler == null) {
                    handlerInfo.addProperty("status", "INVALID");
                    continue;
                }
                handlerInfo.addProperty("class", channelHandler.getClass().getName());
                try {
                    Object child = ReflectionUtil.get(channelHandler, "childHandler", ChannelInitializer.class);
                    handlerInfo.addProperty("childClass", child.getClass().getName());
                    if (child instanceof WrappedChannelInitializer wrappedChannelInitializer) {
                        handlerInfo.addProperty("oldInit", wrappedChannelInitializer.original().getClass().getName());
                    }
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }
        JsonObject wrappedLists = new JsonObject();
        JsonObject currentLists = new JsonObject();
        try {
            for (Pair<Field, Object> pair : injectedLists) {
                Field field = pair.key();
                Object list = field.get(pair.value());
                currentLists.addProperty(field.getName(), list.getClass().getName());
                if (list instanceof SynchronizedListWrapper<?> wrapper) {
                    wrappedLists.addProperty(field.getName(), wrapper.originalList().getClass().getName());
                }
            }
            data.add("wrappedLists", wrappedLists);
            data.add("currentLists", currentLists);
        } catch (ReflectiveOperationException ignored) {
        }
        return data;
    }
    /**
     * Returns the Vanilla server connection object the channels to be injected should be searched in.
     *
     * @return server connection object, or null if failed
     */
    protected abstract @Nullable Object getServerConnection() throws ReflectiveOperationException;
    /**
     * Returns a new Via channel initializer wrapping the original one.
     *
     * @param oldInitializer original channel initializer
     * @return wrapped Via channel initializer
     */
    protected abstract WrappedChannelInitializer createChannelInitializer(ChannelInitializer<Channel> oldInitializer);
    /**
     * Should throw a {@link RuntimeException} with information on what/who might have caused an issue.
     * Called when injection fails.
     *
     * @param bootstrapAcceptor head channel handler to be used when blaming
     * @throws ReflectiveOperationException during reflective operation
     */
    protected abstract void blame(ChannelHandler bootstrapAcceptor) throws ReflectiveOperationException;
}
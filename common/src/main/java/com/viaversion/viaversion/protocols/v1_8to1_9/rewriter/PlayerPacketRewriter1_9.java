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
package com.viaversion.viaversion.protocols.v1_8to1_9.rewriter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.GameMode;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_9;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.CommandBlockProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.CompressionProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.provider.MainHandProvider;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.ClientChunks;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.EntityTracker1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.MovementTracker;
import com.viaversion.viaversion.util.ComponentUtil;
public class PlayerPacketRewriter1_9 {
    public static void register(Protocol1_8To1_9 protocol) {
        protocol.registerClientbound(ClientboundPackets1_8.CHAT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING, Protocol1_8To1_9.STRING_TO_JSON); 
                map(Types.BYTE); 
                handler(wrapper -> {
                    JsonObject obj = (JsonObject) wrapper.get(Types.COMPONENT, 0);
                    if (obj.get("translate") != null && obj.get("translate").getAsString().equals("gameMode.changed")) {
                        EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                        String gameMode = tracker.getGameMode().text();
                        JsonObject gameModeObject = new JsonObject();
                        gameModeObject.addProperty("text", gameMode);
                        gameModeObject.addProperty("color", "gray");
                        gameModeObject.addProperty("italic", true);
                        JsonArray array = new JsonArray();
                        array.add(gameModeObject);
                        obj.add("with", array);
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.TAB_LIST, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING, Protocol1_8To1_9.STRING_TO_JSON); 
                map(Types.STRING, Protocol1_8To1_9.STRING_TO_JSON); 
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.DISCONNECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING, Protocol1_8To1_9.STRING_TO_JSON); 
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.SET_TITLES, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                handler(wrapper -> {
                    int action = wrapper.get(Types.VAR_INT, 0);
                    if (action == 0 || action == 1) {
                        Protocol1_8To1_9.STRING_TO_JSON.write(wrapper, wrapper.read(Types.STRING));
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.PLAYER_POSITION, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.FLOAT); 
                map(Types.FLOAT); 
                map(Types.BYTE); 
                create(Types.VAR_INT, 0); 
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.SET_PLAYER_TEAM, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); 
                map(Types.BYTE); 
                handler(wrapper -> {
                    byte mode = wrapper.get(Types.BYTE, 0); 
                    if (mode == 0 || mode == 2) {
                        wrapper.passthrough(Types.STRING); 
                        wrapper.passthrough(Types.STRING); 
                        wrapper.passthrough(Types.STRING); 
                        wrapper.passthrough(Types.BYTE); 
                        wrapper.passthrough(Types.STRING); 
                        wrapper.write(Types.STRING, Via.getConfig().isPreventCollision() ? "never" : "");
                        wrapper.passthrough(Types.BYTE); 
                    }
                    if (mode == 0 || mode == 3 || mode == 4) {
                        String[] players = wrapper.passthrough(Types.STRING_ARRAY); 
                        final EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                        String myName = wrapper.user().getProtocolInfo().getUsername();
                        String teamName = wrapper.get(Types.STRING, 0);
                        for (String player : players) {
                            if (entityTracker.isAutoTeam() && player.equalsIgnoreCase(myName)) {
                                if (mode == 4) {
                                    wrapper.send(Protocol1_8To1_9.class);
                                    wrapper.cancel();
                                    entityTracker.sendTeamPacket(true, true);
                                    entityTracker.setCurrentTeam("viaversion");
                                } else {
                                    entityTracker.sendTeamPacket(false, true);
                                    entityTracker.setCurrentTeam(teamName);
                                }
                            }
                        }
                    }
                    if (mode == 1) { 
                        final EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                        String teamName = wrapper.get(Types.STRING, 0);
                        if (entityTracker.isAutoTeam()
                            && teamName.equals(entityTracker.getCurrentTeam())) {
                            wrapper.send(Protocol1_8To1_9.class);
                            wrapper.cancel();
                            entityTracker.sendTeamPacket(true, true);
                            entityTracker.setCurrentTeam("viaversion");
                        }
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); 
                handler(wrapper -> {
                    int entityId = wrapper.get(Types.INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.addEntity(entityId, EntityTypes1_9.EntityType.PLAYER);
                    tracker.setClientEntityId(entityId);
                });
                map(Types.UNSIGNED_BYTE); 
                map(Types.BYTE); 
                map(Types.UNSIGNED_BYTE); 
                map(Types.UNSIGNED_BYTE); 
                map(Types.STRING); 
                map(Types.BOOLEAN); 
                handler(wrapper -> {
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    short gamemodeId = wrapper.get(Types.UNSIGNED_BYTE, 0);
                    gamemodeId &= -9; 
                    tracker.setGameMode(GameMode.getById(gamemodeId)); 
                });
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Types.BYTE, 0);
                    clientWorld.setEnvironment(dimensionId);
                });
                handler(wrapper -> {
                    CommandBlockProvider provider = Via.getManager().getProviders().get(CommandBlockProvider.class);
                    provider.sendPermission(wrapper.user());
                });
                handler(wrapper -> {
                    EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    if (Via.getConfig().isAutoTeam()) {
                        entityTracker.setAutoTeam(true);
                        wrapper.send(Protocol1_8To1_9.class);
                        wrapper.cancel();
                        entityTracker.sendTeamPacket(true, true);
                        entityTracker.setCurrentTeam("viaversion");
                    } else {
                        entityTracker.setAutoTeam(false);
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.PLAYER_INFO, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.VAR_INT); 
                handler(wrapper -> {
                    int action = wrapper.get(Types.VAR_INT, 0);
                    int count = wrapper.get(Types.VAR_INT, 1);
                    for (int i = 0; i < count; i++) {
                        wrapper.passthrough(Types.UUID); 
                        if (action == 0) { 
                            wrapper.passthrough(Types.STRING); 
                            int properties = wrapper.passthrough(Types.VAR_INT);
                            for (int j = 0; j < properties; j++) {
                                wrapper.passthrough(Types.STRING); 
                                wrapper.passthrough(Types.STRING); 
                                wrapper.passthrough(Types.OPTIONAL_STRING); 
                            }
                            wrapper.passthrough(Types.VAR_INT); 
                            wrapper.passthrough(Types.VAR_INT); 
                            String displayName = wrapper.read(Types.OPTIONAL_STRING);
                            wrapper.write(Types.OPTIONAL_COMPONENT, displayName != null ?
                                Protocol1_8To1_9.STRING_TO_JSON.transform(wrapper, displayName) : null);
                        } else if ((action == 1) || (action == 2)) { 
                            wrapper.passthrough(Types.VAR_INT);
                        } else if (action == 3) { 
                            String displayName = wrapper.read(Types.OPTIONAL_STRING);
                            wrapper.write(Types.OPTIONAL_COMPONENT, displayName != null ?
                                Protocol1_8To1_9.STRING_TO_JSON.transform(wrapper, displayName) : null);
                        }
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.CUSTOM_PAYLOAD, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); 
                handlerSoftFail(wrapper -> {
                    final String name = wrapper.get(Types.STRING, 0);
                    if (name.equals("MC|BOpen")) {
                        wrapper.write(Types.VAR_INT, 0);
                    } else if (name.equals("MC|TrList")) {
                        protocol.getItemRewriter().handleTradeList(wrapper);
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); 
                map(Types.UNSIGNED_BYTE); 
                map(Types.UNSIGNED_BYTE); 
                map(Types.STRING); 
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Types.INT, 0);
                    clientWorld.setEnvironment(dimensionId);
                });
                handler(wrapper -> {
                    wrapper.user().get(ClientChunks.class).getLoadedChunks().clear();
                    int gamemode = wrapper.get(Types.UNSIGNED_BYTE, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.setGameMode(GameMode.getById(gamemode));
                });
                handler(wrapper -> {
                    CommandBlockProvider provider = Via.getManager().getProviders().get(CommandBlockProvider.class);
                    provider.sendPermission(wrapper.user());
                    provider.unloadChunks(wrapper.user());
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.GAME_EVENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); 
                map(Types.FLOAT); 
                handler(wrapper -> {
                    short reason = wrapper.get(Types.UNSIGNED_BYTE, 0);
                    if (reason == 3) { 
                        int gamemode = wrapper.get(Types.FLOAT, 0).intValue();
                        EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                        tracker.setGameMode(GameMode.getById(gamemode));
                    } else if (reason == 4) { 
                        wrapper.set(Types.FLOAT, 0, 1F);
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.SET_COMPRESSION, null, wrapper -> {
            wrapper.cancel();
            CompressionProvider provider = Via.getManager().getProviders().get(CompressionProvider.class);
            provider.handlePlayCompression(wrapper.user(), wrapper.read(Types.VAR_INT));
        });
        protocol.registerServerbound(ServerboundPackets1_9.COMMAND_SUGGESTION, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); 
                read(Types.BOOLEAN); 
            }
        });
        protocol.registerServerbound(ServerboundPackets1_9.CLIENT_INFORMATION, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); 
                map(Types.BYTE); 
                map(Types.VAR_INT, Types.BYTE); 
                map(Types.BOOLEAN); 
                map(Types.UNSIGNED_BYTE); 
                handler(wrapper -> {
                    int hand = wrapper.read(Types.VAR_INT);
                    if (Via.getConfig().isLeftHandedHandling() && hand == 0) {
                        wrapper.set(Types.UNSIGNED_BYTE, 0, (short) (wrapper.get(Types.UNSIGNED_BYTE, 0).intValue() | 0x80));
                    }
                    wrapper.sendToServer(Protocol1_8To1_9.class);
                    wrapper.cancel();
                    Via.getManager().getProviders().get(MainHandProvider.class).setMainHand(wrapper.user(), hand);
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_9.SWING, new PacketHandlers() {
            @Override
            public void register() {
                read(Types.VAR_INT); 
            }
        });
        protocol.cancelServerbound(ServerboundPackets1_9.ACCEPT_TELEPORTATION);
        protocol.cancelServerbound(ServerboundPackets1_9.MOVE_VEHICLE);
        protocol.cancelServerbound(ServerboundPackets1_9.PADDLE_BOAT);
        protocol.registerServerbound(ServerboundPackets1_9.CUSTOM_PAYLOAD, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); 
                handler(wrapper -> {
                    String name = wrapper.get(Types.STRING, 0);
                    if (name.equals("MC|BSign")) {
                        Item item = wrapper.passthrough(Types.ITEM1_8);
                        if (item != null) {
                            item.setIdentifier(387); 
                            CompoundTag tag = item.tag();
                            ListTag<StringTag> pages = tag.getListTag("pages", StringTag.class);
                            if (pages == null) {
                                return;
                            }
                            for (int i = 0; i < pages.size(); i++) {
                                final StringTag pageTag = pages.get(i);
                                final String value = pageTag.getValue();
                                pageTag.setValue(ComponentUtil.plainToJson(value).toString());
                            }
                        }
                    }
                    if (name.equals("MC|AutoCmd")) {
                        wrapper.set(Types.STRING, 0, "MC|AdvCdm");
                        wrapper.write(Types.BYTE, (byte) 0);
                        wrapper.passthrough(Types.INT); 
                        wrapper.passthrough(Types.INT); 
                        wrapper.passthrough(Types.INT); 
                        wrapper.passthrough(Types.STRING); 
                        wrapper.passthrough(Types.BOOLEAN); 
                        wrapper.clearInputBuffer();
                    }
                    if (name.equals("MC|AdvCmd")) {
                        wrapper.set(Types.STRING, 0, "MC|AdvCdm");
                    }
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_9.CLIENT_COMMAND, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                handler(wrapper -> {
                    int action = wrapper.get(Types.VAR_INT, 0);
                    if (action == 2) {
                        EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                        if (tracker.isBlocking()) {
                            if (!Via.getConfig().isShowShieldWhenSwordInHand()) {
                                tracker.setSecondHand(null);
                            }
                            tracker.setBlocking(false);
                        }
                    }
                });
            }
        });
        final PacketHandler onGroundHandler = wrapper -> {
            final MovementTracker tracker = wrapper.user().get(MovementTracker.class);
            tracker.incrementIdlePacket();
            tracker.setGround(wrapper.get(Types.BOOLEAN, 0));
        };
        protocol.registerServerbound(ServerboundPackets1_9.MOVE_PLAYER_POS, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.BOOLEAN); 
                handler(onGroundHandler);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_9.MOVE_PLAYER_POS_ROT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.FLOAT); 
                map(Types.FLOAT); 
                map(Types.BOOLEAN); 
                handler(onGroundHandler);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_9.MOVE_PLAYER_ROT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.FLOAT); 
                map(Types.FLOAT); 
                map(Types.BOOLEAN); 
                handler(onGroundHandler);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_9.MOVE_PLAYER_STATUS_ONLY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BOOLEAN); 
                handler(onGroundHandler);
            }
        });
    }
}
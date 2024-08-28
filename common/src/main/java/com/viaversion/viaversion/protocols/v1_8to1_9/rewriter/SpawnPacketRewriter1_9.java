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
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_8;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_9;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_9;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.api.type.types.version.Types1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.EntityTracker1_9;
import java.util.ArrayList;
import java.util.List;
public class SpawnPacketRewriter1_9 {
    public static final ValueTransformer<Integer, Double> toNewDouble = new ValueTransformer<>(Types.DOUBLE) {
        @Override
        public Double transform(PacketWrapper wrapper, Integer inputValue) {
            return inputValue / 32D;
        }
    };
    public static void register(Protocol1_8To1_9 protocol) {
        protocol.registerClientbound(ClientboundPackets1_8.ADD_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    wrapper.write(Types.UUID, tracker.getEntityUUID(entityID)); 
                });
                map(Types.BYTE); 
                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    int typeID = wrapper.get(Types.BYTE, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.addEntity(entityID, EntityTypes1_9.getTypeFromId(typeID, true));
                });
                map(Types.INT, toNewDouble); 
                map(Types.INT, toNewDouble); 
                map(Types.INT, toNewDouble); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types.INT); 
                handler(wrapper -> {
                    int data = wrapper.get(Types.INT, 0); 
                    short vX = 0;
                    short vY = 0;
                    short vZ = 0;
                    if (data > 0) {
                        vX = wrapper.read(Types.SHORT);
                        vY = wrapper.read(Types.SHORT);
                        vZ = wrapper.read(Types.SHORT);
                    }
                    wrapper.write(Types.SHORT, vX);
                    wrapper.write(Types.SHORT, vY);
                    wrapper.write(Types.SHORT, vZ);
                });
                handler(wrapper -> {
                    final int entityID = wrapper.get(Types.VAR_INT, 0);
                    final int data = wrapper.get(Types.INT, 0); 
                    int typeID = wrapper.get(Types.BYTE, 0);
                    if (EntityTypes1_8.getTypeFromId(typeID, true) == EntityTypes1_8.EntityType.POTION) {
                        PacketWrapper entityDataPacket = wrapper.create(ClientboundPackets1_9.SET_ENTITY_DATA, wrapper1 -> {
                            wrapper1.write(Types.VAR_INT, entityID);
                            List<EntityData> entityData = new ArrayList<>();
                            Item item = new DataItem(373, (byte) 1, (short) data, null); 
                            protocol.getItemRewriter().handleItemToClient(wrapper.user(), item); 
                            EntityData potion = new EntityData(5, EntityDataTypes1_9.ITEM, item);
                            entityData.add(potion);
                            wrapper1.write(Types1_9.ENTITY_DATA_LIST, entityData);
                        });
                        wrapper.send(Protocol1_8To1_9.class);
                        entityDataPacket.send(Protocol1_8To1_9.class);
                        wrapper.cancel();
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.ADD_EXPERIENCE_ORB, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.addEntity(entityID, EntityTypes1_9.EntityType.EXPERIENCE_ORB);
                });
                map(Types.INT, toNewDouble); 
                map(Types.INT, toNewDouble); 
                map(Types.INT, toNewDouble); 
                map(Types.SHORT); 
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.ADD_GLOBAL_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.BYTE); 
                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.addEntity(entityID, EntityTypes1_9.EntityType.LIGHTNING_BOLT);
                });
                map(Types.INT, toNewDouble); 
                map(Types.INT, toNewDouble); 
                map(Types.INT, toNewDouble); 
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.ADD_MOB, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    wrapper.write(Types.UUID, tracker.getEntityUUID(entityID)); 
                });
                map(Types.UNSIGNED_BYTE); 
                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    int typeID = wrapper.get(Types.UNSIGNED_BYTE, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.addEntity(entityID, EntityTypes1_9.getTypeFromId(typeID, false));
                });
                map(Types.INT, toNewDouble); 
                map(Types.INT, toNewDouble); 
                map(Types.INT, toNewDouble); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types.SHORT); 
                map(Types.SHORT); 
                map(Types.SHORT); 
                map(Types1_8.ENTITY_DATA_LIST, Types1_9.ENTITY_DATA_LIST);
                handler(wrapper -> {
                    List<EntityData> entityDataList = wrapper.get(Types1_9.ENTITY_DATA_LIST, 0);
                    int entityId = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    if (tracker.hasEntity(entityId)) {
                        protocol.getEntityRewriter().handleEntityData(entityId, entityDataList, wrapper.user());
                    } else {
                        protocol.getLogger().warning("Unable to find entity for entity data, entity ID: " + entityId);
                        entityDataList.clear();
                    }
                });
                handler(wrapper -> {
                    List<EntityData> entityDataList = wrapper.get(Types1_9.ENTITY_DATA_LIST, 0);
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.handleEntityData(entityID, entityDataList);
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.ADD_PAINTING, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.addEntity(entityID, EntityTypes1_9.EntityType.PAINTING);
                });
                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    wrapper.write(Types.UUID, tracker.getEntityUUID(entityID)); 
                });
                map(Types.STRING); 
                map(Types.BLOCK_POSITION1_8); 
                map(Types.BYTE); 
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.ADD_PLAYER, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.UUID); 
                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.addEntity(entityID, EntityTypes1_9.EntityType.PLAYER);
                });
                map(Types.INT, toNewDouble); 
                map(Types.INT, toNewDouble); 
                map(Types.INT, toNewDouble); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                handler(wrapper -> {
                    short item = wrapper.read(Types.SHORT);
                    if (item != 0) {
                        PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_9.SET_EQUIPPED_ITEM, null, wrapper.user());
                        packet.write(Types.VAR_INT, wrapper.get(Types.VAR_INT, 0));
                        packet.write(Types.VAR_INT, 0);
                        packet.write(Types.ITEM1_8, new DataItem(item, (byte) 1, (short) 0, null));
                        packet.send(Protocol1_8To1_9.class);
                    }
                });
                map(Types1_8.ENTITY_DATA_LIST, Types1_9.ENTITY_DATA_LIST);
                handler(wrapper -> {
                    List<EntityData> entityDataList = wrapper.get(Types1_9.ENTITY_DATA_LIST, 0);
                    int entityId = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    if (tracker.hasEntity(entityId)) {
                        protocol.getEntityRewriter().handleEntityData(entityId, entityDataList, wrapper.user());
                    } else {
                        protocol.getLogger().warning("Unable to find entity for entity data, entity ID: " + entityId);
                        entityDataList.clear();
                    }
                });
                handler(wrapper -> {
                    List<EntityData> entityDataList = wrapper.get(Types1_9.ENTITY_DATA_LIST, 0);
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    tracker.handleEntityData(entityID, entityDataList);
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.REMOVE_ENTITIES, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT_ARRAY_PRIMITIVE); 
                handler(wrapper -> {
                    int[] entities = wrapper.get(Types.VAR_INT_ARRAY_PRIMITIVE, 0);
                    EntityTracker tracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                    for (int entity : entities) {
                        tracker.removeEntity(entity);
                    }
                });
            }
        });
    }
}

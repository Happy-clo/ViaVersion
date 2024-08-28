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
package com.viaversion.viaversion.protocols.v1_12_2to1_13.rewriter;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_12;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.data.EntityIdMappings1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.data.ParticleIdMappings1_13;
import com.viaversion.viaversion.protocols.v1_12to1_12_1.packet.ClientboundPackets1_12_1;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
public class EntityPacketRewriter1_13 extends EntityRewriter<ClientboundPackets1_12_1, Protocol1_12_2To1_13> {
    public EntityPacketRewriter1_13(Protocol1_12_2To1_13 protocol) {
        super(protocol);
    }
    @Override
    protected void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_12_1.ADD_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.UUID); 
                map(Types.BYTE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types.INT); 
                handler(wrapper -> {
                    int entityId = wrapper.get(Types.VAR_INT, 0);
                    byte type = wrapper.get(Types.BYTE, 0);
                    EntityTypes1_13.EntityType entType = EntityTypes1_13.getTypeFromId(type, true);
                    if (entType == null) return;
                    wrapper.user().getEntityTracker(Protocol1_12_2To1_13.class).addEntity(entityId, entType);
                    if (entType.is(EntityTypes1_13.EntityType.FALLING_BLOCK)) {
                        int oldId = wrapper.get(Types.INT, 0);
                        int combined = (((oldId & 4095) << 4) | (oldId >> 12 & 15));
                        wrapper.set(Types.INT, 0, WorldPacketRewriter1_13.toNewId(combined));
                    }
                    if (entType.is(EntityTypes1_13.EntityType.ITEM_FRAME)) {
                        int data = wrapper.get(Types.INT, 0);
                        switch (data) {
                            case 0 -> data = 3; 
                            case 1 -> data = 4; 
                            case 3 -> data = 5; 
                        }
                        wrapper.set(Types.INT, 0, data);
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_12_1.ADD_MOB, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.UUID); 
                map(Types.VAR_INT); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types.SHORT); 
                map(Types.SHORT); 
                map(Types.SHORT); 
                map(Types1_12.ENTITY_DATA_LIST, Types1_13.ENTITY_DATA_LIST); 
                handler(trackerAndRewriterHandler(Types1_13.ENTITY_DATA_LIST));
            }
        });
        protocol.registerClientbound(ClientboundPackets1_12_1.ADD_PLAYER, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.UUID); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types1_12.ENTITY_DATA_LIST, Types1_13.ENTITY_DATA_LIST); 
                handler(trackerAndRewriterHandler(Types1_13.ENTITY_DATA_LIST, EntityTypes1_13.EntityType.PLAYER));
            }
        });
        protocol.registerClientbound(ClientboundPackets1_12_1.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); 
                map(Types.UNSIGNED_BYTE); 
                map(Types.INT); 
                handler(wrapper -> {
                    ClientWorld clientChunks = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Types.INT, 1);
                    clientChunks.setEnvironment(dimensionId);
                });
                handler(playerTrackerHandler());
                handler(Protocol1_12_2To1_13.SEND_DECLARE_COMMANDS_AND_TAGS);
            }
        });
        protocol.registerClientbound(ClientboundPackets1_12_1.UPDATE_MOB_EFFECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types.VAR_INT); 
                handler(packetWrapper -> {
                    byte flags = packetWrapper.read(Types.BYTE); 
                    if (Via.getConfig().isNewEffectIndicator())
                        flags |= 0x04;
                    packetWrapper.write(Types.BYTE, flags);
                });
            }
        });
        registerRemoveEntities(ClientboundPackets1_12_1.REMOVE_ENTITIES);
        registerSetEntityData(ClientboundPackets1_12_1.SET_ENTITY_DATA, Types1_12.ENTITY_DATA_LIST, Types1_13.ENTITY_DATA_LIST);
    }
    @Override
    protected void registerRewrites() {
        filter().mapDataType(typeId -> Types1_13.ENTITY_DATA_TYPES.byId(typeId > 4 ? typeId + 1 : typeId));
        filter().dataType(Types1_13.ENTITY_DATA_TYPES.itemType).handler(((event, data) -> protocol.getItemRewriter().handleItemToClient(event.user(), data.value())));
        filter().dataType(Types1_13.ENTITY_DATA_TYPES.optionalBlockStateType).handler(((event, data) -> {
            final int oldId = data.value();
            if (oldId != 0) {
                final int combined = (((oldId & 4095) << 4) | (oldId >> 12 & 15));
                final int newId = WorldPacketRewriter1_13.toNewId(combined);
                data.setValue(newId);
            }
        }));
        filter().index(0).handler((event, data) -> data.setValue((byte) ((byte) data.getValue() & ~0x10)));
        filter().index(2).handler(((event, data) -> {
            if (data.getValue() != null && !((String) data.getValue()).isEmpty()) {
                data.setTypeAndValue(Types1_13.ENTITY_DATA_TYPES.optionalComponentType, ComponentUtil.legacyToJson((String) data.getValue()));
            } else {
                data.setTypeAndValue(Types1_13.ENTITY_DATA_TYPES.optionalComponentType, null);
            }
        }));
        filter().type(EntityTypes1_13.EntityType.WOLF).index(17).handler((event, data) -> {
            data.setValue(15 - (int) data.getValue());
        });
        filter().type(EntityTypes1_13.EntityType.ZOMBIE).addIndex(15); 
        filter().type(EntityTypes1_13.EntityType.ABSTRACT_MINECART).index(9).handler((event, data) -> {
            final int oldId = data.value();
            final int combined = (((oldId & 4095) << 4) | (oldId >> 12 & 15));
            final int newId = WorldPacketRewriter1_13.toNewId(combined);
            data.setValue(newId);
        });
        filter().type(EntityTypes1_13.EntityType.AREA_EFFECT_CLOUD).handler((event, data) -> {
            if (data.id() == 9) {
                int particleId = data.value();
                EntityData parameter1Data = event.dataAtIndex(10);
                EntityData parameter2Data = event.dataAtIndex(11);
                int parameter1 = parameter1Data != null ? parameter1Data.value() : 0;
                int parameter2 = parameter2Data != null ? parameter2Data.value() : 0;
                Particle particle = ParticleIdMappings1_13.rewriteParticle(particleId, new Integer[]{parameter1, parameter2});
                if (particle != null && particle.id() != -1) {
                    event.createExtraData(new EntityData(9, Types1_13.ENTITY_DATA_TYPES.particleType, particle));
                }
            }
            if (data.id() >= 9) {
                event.cancel();
            }
        });
    }
    @Override
    public int newEntityId(final int id) {
        return EntityIdMappings1_13.getNewId(id);
    }
    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_13.getTypeFromId(type, false);
    }
    @Override
    public EntityType objectTypeFromId(int type) {
        return EntityTypes1_13.getTypeFromId(type, true);
    }
}
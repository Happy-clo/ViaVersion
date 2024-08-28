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
package com.viaversion.viaversion.protocols.v1_14_4to1_15.rewriter;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_15;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.protocols.v1_14_3to1_14_4.packet.ClientboundPackets1_14_4;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.Protocol1_14_4To1_15;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.packet.ClientboundPackets1_15;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import java.util.List;
public class EntityPacketRewriter1_15 extends EntityRewriter<ClientboundPackets1_14_4, Protocol1_14_4To1_15> {
    public EntityPacketRewriter1_15(Protocol1_14_4To1_15 protocol) {
        super(protocol);
    }
    @Override
    protected void registerPackets() {
        registerTrackerWithData(ClientboundPackets1_14_4.ADD_ENTITY, EntityTypes1_15.FALLING_BLOCK);
        protocol.registerClientbound(ClientboundPackets1_14_4.ADD_MOB, new PacketHandlers() {
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
                handler(trackerHandler());
                handler(wrapper -> sendEntityDataPacket(wrapper, wrapper.get(Types.VAR_INT, 0)));
            }
        });
        protocol.registerClientbound(ClientboundPackets1_14_4.ADD_PLAYER, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.UUID); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                handler(wrapper -> {
                    int entityId = wrapper.get(Types.VAR_INT, 0);
                    wrapper.user().getEntityTracker(Protocol1_14_4To1_15.class).addEntity(entityId, EntityTypes1_15.PLAYER);
                    sendEntityDataPacket(wrapper, entityId);
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_14_4.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT);
                handler(wrapper -> wrapper.write(Types.LONG, 0L)); 
            }
        });
        protocol.registerClientbound(ClientboundPackets1_14_4.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); 
                map(Types.UNSIGNED_BYTE); 
                map(Types.INT); 
                handler(playerTrackerHandler());
                handler(wrapper -> wrapper.write(Types.LONG, 0L)); 
                map(Types.UNSIGNED_BYTE); 
                map(Types.STRING); 
                map(Types.VAR_INT); 
                map(Types.BOOLEAN); 
                handler(wrapper -> wrapper.write(Types.BOOLEAN, !Via.getConfig().is1_15InstantRespawn())); 
            }
        });
        registerSetEntityData(ClientboundPackets1_14_4.SET_ENTITY_DATA, Types1_14.ENTITY_DATA_LIST);
        registerRemoveEntities(ClientboundPackets1_14_4.REMOVE_ENTITIES);
    }
    @Override
    protected void registerRewrites() {
        registerEntityDataTypeHandler(Types1_14.ENTITY_DATA_TYPES.itemType, Types1_14.ENTITY_DATA_TYPES.optionalBlockStateType, Types1_14.ENTITY_DATA_TYPES.particleType);
        registerBlockStateHandler(EntityTypes1_15.ABSTRACT_MINECART, 10);
        filter().type(EntityTypes1_15.LIVING_ENTITY).addIndex(12);
        filter().type(EntityTypes1_15.WOLF).removeIndex(18);
    }
    private void sendEntityDataPacket(PacketWrapper wrapper, int entityId) {
        List<EntityData> entityData = wrapper.read(Types1_14.ENTITY_DATA_LIST);
        if (entityData.isEmpty()) {
            return;
        }
        wrapper.send(Protocol1_14_4To1_15.class);
        wrapper.cancel();
        handleEntityData(entityId, entityData, wrapper.user());
        PacketWrapper entityDataPacket = PacketWrapper.create(ClientboundPackets1_15.SET_ENTITY_DATA, wrapper.user());
        entityDataPacket.write(Types.VAR_INT, entityId);
        entityDataPacket.write(Types1_14.ENTITY_DATA_LIST, entityData);
        entityDataPacket.send(Protocol1_14_4To1_15.class);
    }
    @Override
    public int newEntityId(final int id) {
        return id >= 4 ? id + 1 : id; 
    }
    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_15.getTypeFromId(type);
    }
}

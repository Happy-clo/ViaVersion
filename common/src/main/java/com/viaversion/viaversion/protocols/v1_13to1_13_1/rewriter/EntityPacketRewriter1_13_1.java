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
package com.viaversion.viaversion.protocols.v1_13to1_13_1.rewriter;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13to1_13_1.Protocol1_13To1_13_1;
import com.viaversion.viaversion.rewriter.EntityRewriter;
public class EntityPacketRewriter1_13_1 extends EntityRewriter<ClientboundPackets1_13, Protocol1_13To1_13_1> {
    public EntityPacketRewriter1_13_1(Protocol1_13To1_13_1 protocol) {
        super(protocol);
    }
    @Override
    protected void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_13.ADD_ENTITY, new PacketHandlers() {
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
                    if (entType != null) {
                        if (entType.is(EntityTypes1_13.EntityType.FALLING_BLOCK)) {
                            int data = wrapper.get(Types.INT, 0);
                            wrapper.set(Types.INT, 0, protocol.getMappingData().getNewBlockStateId(data));
                        }
                        wrapper.user().getEntityTracker(Protocol1_13To1_13_1.class).addEntity(entityId, entType);
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.ADD_MOB, new PacketHandlers() {
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
                map(Types1_13.ENTITY_DATA_LIST); 
                handler(trackerAndRewriterHandler(Types1_13.ENTITY_DATA_LIST));
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.ADD_PLAYER, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.UUID); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types1_13.ENTITY_DATA_LIST); 
                handler(trackerAndRewriterHandler(Types1_13.ENTITY_DATA_LIST, EntityTypes1_13.EntityType.PLAYER));
            }
        });
        registerRemoveEntities(ClientboundPackets1_13.REMOVE_ENTITIES);
        registerSetEntityData(ClientboundPackets1_13.SET_ENTITY_DATA, Types1_13.ENTITY_DATA_LIST);
    }
    @Override
    protected void registerRewrites() {
        registerEntityDataTypeHandler(Types1_13.ENTITY_DATA_TYPES.itemType, Types1_13.ENTITY_DATA_TYPES.optionalBlockStateType, Types1_13.ENTITY_DATA_TYPES.particleType);
        registerBlockStateHandler(EntityTypes1_13.EntityType.ABSTRACT_MINECART, 9);
        filter().type(EntityTypes1_13.EntityType.ABSTRACT_ARROW).addIndex(7); 
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

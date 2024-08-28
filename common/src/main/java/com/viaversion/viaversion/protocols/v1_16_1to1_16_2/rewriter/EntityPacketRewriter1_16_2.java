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
package com.viaversion.viaversion.protocols.v1_16_1to1_16_2.rewriter;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_16_2;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.Protocol1_16_1To1_16_2;
import com.viaversion.viaversion.rewriter.EntityRewriter;
public class EntityPacketRewriter1_16_2 extends EntityRewriter<ClientboundPackets1_16, Protocol1_16_1To1_16_2> {
    public EntityPacketRewriter1_16_2(Protocol1_16_1To1_16_2 protocol) {
        super(protocol);
    }
    @Override
    protected void registerPackets() {
        registerTrackerWithData(ClientboundPackets1_16.ADD_ENTITY, EntityTypes1_16_2.FALLING_BLOCK);
        registerTracker(ClientboundPackets1_16.ADD_MOB);
        registerTracker(ClientboundPackets1_16.ADD_PLAYER, EntityTypes1_16_2.PLAYER);
        registerSetEntityData(ClientboundPackets1_16.SET_ENTITY_DATA, Types1_16.ENTITY_DATA_LIST);
        registerRemoveEntities(ClientboundPackets1_16.REMOVE_ENTITIES);
        protocol.registerClientbound(ClientboundPackets1_16.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); 
                handler(wrapper -> {
                    short gamemode = wrapper.read(Types.UNSIGNED_BYTE);
                    wrapper.write(Types.BOOLEAN, (gamemode & 0x08) != 0); 
                    wrapper.write(Types.BYTE, (byte) (gamemode & ~0x08)); 
                });
                map(Types.BYTE); 
                map(Types.STRING_ARRAY); 
                handler(wrapper -> {
                    wrapper.read(Types.NAMED_COMPOUND_TAG);
                    wrapper.write(Types.NAMED_COMPOUND_TAG, protocol.getMappingData().getDimensionRegistry());
                    String dimensionType = wrapper.read(Types.STRING);
                    wrapper.write(Types.NAMED_COMPOUND_TAG, getDimensionData(dimensionType));
                });
                map(Types.STRING); 
                map(Types.LONG); 
                map(Types.UNSIGNED_BYTE, Types.VAR_INT); 
                handler(playerTrackerHandler());
            }
        });
        protocol.registerClientbound(ClientboundPackets1_16.RESPAWN, wrapper -> {
            String dimensionType = wrapper.read(Types.STRING);
            wrapper.write(Types.NAMED_COMPOUND_TAG, getDimensionData(dimensionType));
        });
    }
    @Override
    protected void registerRewrites() {
        registerEntityDataTypeHandler(Types1_16.ENTITY_DATA_TYPES.itemType, Types1_16.ENTITY_DATA_TYPES.optionalBlockStateType, Types1_16.ENTITY_DATA_TYPES.particleType);
        registerBlockStateHandler(EntityTypes1_16_2.ABSTRACT_MINECART, 10);
        filter().type(EntityTypes1_16_2.ABSTRACT_PIGLIN).handler((event, data) -> {
            if (data.id() == 15) {
                data.setId(16);
            } else if (data.id() == 16) {
                data.setId(15);
            }
        });
    }
    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }
    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_16_2.getTypeFromId(type);
    }
    private CompoundTag getDimensionData(String dimensionType) {
        CompoundTag tag = Protocol1_16_1To1_16_2.MAPPINGS.getDimensionDataMap().get(dimensionType);
        if (tag == null) {
            protocol.getLogger().severe("Could not get dimension data of " + dimensionType);
            throw new NullPointerException("Dimension data for " + dimensionType + " is null!");
        }
        return tag.copy();
    }
}

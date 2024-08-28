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
package com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_3;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundPacket1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.Protocol1_20_2To1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Key;
public final class EntityPacketRewriter1_20_3 extends EntityRewriter<ClientboundPacket1_20_2, Protocol1_20_2To1_20_3> {
    public EntityPacketRewriter1_20_3(final Protocol1_20_2To1_20_3 protocol) {
        super(protocol);
    }
    @Override
    public void registerPackets() {
        registerTrackerWithData1_19(ClientboundPackets1_20_2.ADD_ENTITY, EntityTypes1_20_3.FALLING_BLOCK);
        registerSetEntityData(ClientboundPackets1_20_2.SET_ENTITY_DATA, Types1_20_2.ENTITY_DATA_LIST, Types1_20_3.ENTITY_DATA_LIST);
        registerRemoveEntities(ClientboundPackets1_20_2.REMOVE_ENTITIES);
        protocol.registerClientbound(ClientboundConfigurationPackets1_20_2.REGISTRY_DATA, new PacketHandlers() {
            @Override
            protected void register() {
                map(Types.COMPOUND_TAG); 
                handler(configurationDimensionDataHandler());
                handler(configurationBiomeSizeTracker());
            }
        });
        protocol.registerClientbound(ClientboundPackets1_20_2.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); 
                map(Types.BOOLEAN); 
                map(Types.STRING_ARRAY); 
                map(Types.VAR_INT); 
                map(Types.VAR_INT); 
                map(Types.VAR_INT); 
                map(Types.BOOLEAN); 
                map(Types.BOOLEAN); 
                map(Types.BOOLEAN); 
                map(Types.STRING); 
                map(Types.STRING); 
                handler(worldDataTrackerHandlerByKey());
                handler(wrapper -> sendChunksSentGameEvent(wrapper));
            }
        });
        protocol.registerClientbound(ClientboundPackets1_20_2.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); 
                map(Types.STRING); 
                handler(worldDataTrackerHandlerByKey());
                handler(wrapper -> sendChunksSentGameEvent(wrapper));
            }
        });
        protocol.registerClientbound(ClientboundPackets1_20_2.INITIALIZE_BORDER, this::sendChunksSentGameEvent);
    }
    private void sendChunksSentGameEvent(final PacketWrapper wrapper) {
        wrapper.send(Protocol1_20_2To1_20_3.class);
        wrapper.cancel();
        final PacketWrapper gameEventPacket = wrapper.create(ClientboundPackets1_20_3.GAME_EVENT);
        gameEventPacket.write(Types.UNSIGNED_BYTE, (short) 13);
        gameEventPacket.write(Types.FLOAT, 0F);
        gameEventPacket.send(Protocol1_20_2To1_20_3.class);
    }
    @Override
    protected void registerRewrites() {
        filter().handler((event, data) -> {
            final EntityDataType type = data.dataType();
            if (type == Types1_20_2.ENTITY_DATA_TYPES.componentType) {
                data.setTypeAndValue(Types1_20_3.ENTITY_DATA_TYPES.componentType, ComponentUtil.jsonToTag(data.value()));
            } else if (type == Types1_20_2.ENTITY_DATA_TYPES.optionalComponentType) {
                data.setTypeAndValue(Types1_20_3.ENTITY_DATA_TYPES.optionalComponentType, ComponentUtil.jsonToTag(data.value()));
            } else {
                data.setDataType(Types1_20_3.ENTITY_DATA_TYPES.byId(type.typeId()));
            }
        });
        filter().dataType(Types1_20_3.ENTITY_DATA_TYPES.particleType).handler((event, data) -> {
            final Particle particle = data.value();
            final ParticleMappings particleMappings = protocol.getMappingData().getParticleMappings();
            if (particle.id() == particleMappings.id("vibration")) {
                final String resourceLocation = particle.<String>removeArgument(0).getValue();
                if (Key.stripMinecraftNamespace(resourceLocation).equals("block")) {
                    particle.add(0, Types.VAR_INT, 0);
                } else { 
                    particle.add(0, Types.VAR_INT, 1);
                }
            }
        });
        registerEntityDataTypeHandler(
            Types1_20_3.ENTITY_DATA_TYPES.itemType,
            Types1_20_3.ENTITY_DATA_TYPES.blockStateType,
            Types1_20_3.ENTITY_DATA_TYPES.optionalBlockStateType,
            Types1_20_3.ENTITY_DATA_TYPES.particleType,
            null
        );
        registerBlockStateHandler(EntityTypes1_20_3.ABSTRACT_MINECART, 11);
    }
    @Override
    public void onMappingDataLoaded() {
        mapTypes();
    }
    @Override
    public EntityType typeFromId(final int type) {
        return EntityTypes1_20_3.getTypeFromId(type);
    }
}

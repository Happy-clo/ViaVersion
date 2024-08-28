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
package com.viaversion.viaversion.protocols.v1_9_3to1_10;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_9_3;
import com.viaversion.viaversion.api.type.types.version.Types1_9;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ServerboundPackets1_9_3;
import com.viaversion.viaversion.protocols.v1_9_3to1_10.rewriter.ItemPacketRewriter1_10;
import com.viaversion.viaversion.protocols.v1_9_3to1_10.storage.ResourcePackTracker;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
public class Protocol1_9_3To1_10 extends AbstractProtocol<ClientboundPackets1_9_3, ClientboundPackets1_9_3, ServerboundPackets1_9_3, ServerboundPackets1_9_3> {
    public static final ValueTransformer<Short, Float> TO_NEW_PITCH = new ValueTransformer<>(Types.FLOAT) {
        @Override
        public Float transform(PacketWrapper wrapper, Short inputValue) {
            return inputValue / 63.0F;
        }
    };
    public static final ValueTransformer<List<EntityData>, List<EntityData>> TRANSFORM_ENTITY_DATA = new ValueTransformer<>(Types1_9.ENTITY_DATA_LIST) {
        @Override
        public List<EntityData> transform(PacketWrapper wrapper, List<EntityData> inputValue) {
            List<EntityData> dataList = new CopyOnWriteArrayList<>(inputValue);
            for (EntityData data : dataList) {
                if (data.id() >= 5)
                    data.setId(data.id() + 1);
            }
            return dataList;
        }
    };
    private final ItemPacketRewriter1_10 itemRewriter = new ItemPacketRewriter1_10(this);
    public Protocol1_9_3To1_10() {
        super(ClientboundPackets1_9_3.class, ClientboundPackets1_9_3.class, ServerboundPackets1_9_3.class, ServerboundPackets1_9_3.class);
    }
    @Override
    protected void registerPackets() {
        itemRewriter.register();
        registerClientbound(ClientboundPackets1_9_3.CUSTOM_SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); 
                map(Types.VAR_INT); 
                map(Types.INT); 
                map(Types.INT); 
                map(Types.INT); 
                map(Types.FLOAT); 
                map(Types.UNSIGNED_BYTE, TO_NEW_PITCH); 
            }
        });
        registerClientbound(ClientboundPackets1_9_3.SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.VAR_INT); 
                map(Types.INT); 
                map(Types.INT); 
                map(Types.INT); 
                map(Types.FLOAT); 
                map(Types.UNSIGNED_BYTE, TO_NEW_PITCH); 
                handler(wrapper -> {
                    int id = wrapper.get(Types.VAR_INT, 0);
                    wrapper.set(Types.VAR_INT, 0, getNewSoundId(id));
                });
            }
        });
        registerClientbound(ClientboundPackets1_9_3.SET_ENTITY_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types1_9.ENTITY_DATA_LIST, TRANSFORM_ENTITY_DATA); 
            }
        });
        registerClientbound(ClientboundPackets1_9_3.ADD_MOB, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.UUID); 
                map(Types.UNSIGNED_BYTE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types.SHORT); 
                map(Types.SHORT); 
                map(Types.SHORT); 
                map(Types1_9.ENTITY_DATA_LIST, TRANSFORM_ENTITY_DATA); 
            }
        });
        registerClientbound(ClientboundPackets1_9_3.ADD_PLAYER, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.UUID); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types1_9.ENTITY_DATA_LIST, TRANSFORM_ENTITY_DATA); 
            }
        });
        registerClientbound(ClientboundPackets1_9_3.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); 
                map(Types.UNSIGNED_BYTE); 
                map(Types.INT); 
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Types.INT, 1);
                    clientWorld.setEnvironment(dimensionId);
                });
            }
        });
        registerClientbound(ClientboundPackets1_9_3.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); 
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Types.INT, 0);
                    clientWorld.setEnvironment(dimensionId);
                });
            }
        });
        registerClientbound(ClientboundPackets1_9_3.LEVEL_CHUNK, wrapper -> {
            ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
            Chunk chunk = wrapper.passthrough(ChunkType1_9_3.forEnvironment(clientWorld.getEnvironment()));
            if (Via.getConfig().isReplacePistons()) {
                int replacementId = Via.getConfig().getPistonReplacementId();
                for (ChunkSection section : chunk.getSections()) {
                    if (section == null) continue;
                    section.palette(PaletteType.BLOCKS).replaceId(36, replacementId);
                }
            }
        });
        registerClientbound(ClientboundPackets1_9_3.RESOURCE_PACK, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); 
                map(Types.STRING); 
                handler(wrapper -> {
                    ResourcePackTracker tracker = wrapper.user().get(ResourcePackTracker.class);
                    tracker.setLastHash(wrapper.get(Types.STRING, 1)); 
                });
            }
        });
        registerServerbound(ServerboundPackets1_9_3.RESOURCE_PACK, new PacketHandlers() {
            @Override
            public void register() {
                handler(wrapper -> {
                    ResourcePackTracker tracker = wrapper.user().get(ResourcePackTracker.class);
                    wrapper.write(Types.STRING, tracker.getLastHash());
                    wrapper.write(Types.VAR_INT, wrapper.read(Types.VAR_INT));
                });
            }
        });
    }
    public int getNewSoundId(int id) {
        int newId = id;
        if (id >= 24) 
            newId += 1;
        if (id >= 248) 
            newId += 4;
        if (id >= 296) 
            newId += 6;
        if (id >= 354) 
            newId += 4;
        if (id >= 372) 
            newId += 4;
        return newId;
    }
    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new ResourcePackTracker());
        if (!userConnection.has(ClientWorld.class)) {
            userConnection.put(new ClientWorld());
        }
    }
    @Override
    public ItemPacketRewriter1_10 getItemRewriter() {
        return itemRewriter;
    }
}

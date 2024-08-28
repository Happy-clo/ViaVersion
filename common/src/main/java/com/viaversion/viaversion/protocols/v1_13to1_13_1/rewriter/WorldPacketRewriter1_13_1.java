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
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13to1_13_1.Protocol1_13To1_13_1;
import com.viaversion.viaversion.rewriter.BlockRewriter;
public class WorldPacketRewriter1_13_1 {
    public static void register(Protocol1_13To1_13_1 protocol) {
        BlockRewriter<ClientboundPackets1_13> blockRewriter = BlockRewriter.legacy(protocol);
        protocol.registerClientbound(ClientboundPackets1_13.LEVEL_CHUNK, wrapper -> {
            ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
            Chunk chunk = wrapper.passthrough(ChunkType1_13.forEnvironment(clientWorld.getEnvironment()));
            blockRewriter.handleChunk(chunk);
        });
        blockRewriter.registerBlockEvent(ClientboundPackets1_13.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_13.BLOCK_UPDATE);
        blockRewriter.registerChunkBlocksUpdate(ClientboundPackets1_13.CHUNK_BLOCKS_UPDATE);
        protocol.registerClientbound(ClientboundPackets1_13.LEVEL_EVENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); 
                map(Types.BLOCK_POSITION1_8); 
                map(Types.INT); 
                handler(wrapper -> {
                    int id = wrapper.get(Types.INT, 0);
                    if (id == 2000) { 
                        int data = wrapper.get(Types.INT, 1);
                        switch (data) {
                            case 1: 
                                wrapper.set(Types.INT, 1, 2); 
                                break;
                            case 0: 
                            case 3: 
                            case 6: 
                                wrapper.set(Types.INT, 1, 4); 
                                break;
                            case 2: 
                            case 5: 
                            case 8: 
                                wrapper.set(Types.INT, 1, 5); 
                                break;
                            case 7: 
                                wrapper.set(Types.INT, 1, 3); 
                                break;
                            default: 
                                wrapper.set(Types.INT, 1, 0); 
                                break;
                        }
                    } else if (id == 1010) { 
                        wrapper.set(Types.INT, 1, protocol.getMappingData().getNewItemId(wrapper.get(Types.INT, 1)));
                    } else if (id == 2001) { 
                        wrapper.set(Types.INT, 1, protocol.getMappingData().getNewBlockStateId(wrapper.get(Types.INT, 1)));
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.LOGIN, new PacketHandlers() {
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
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.RESPAWN, new PacketHandlers() {
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
    }
}

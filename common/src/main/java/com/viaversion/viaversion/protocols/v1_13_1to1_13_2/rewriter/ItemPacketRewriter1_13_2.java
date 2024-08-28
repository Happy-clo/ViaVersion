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
package com.viaversion.viaversion.protocols.v1_13_1to1_13_2.rewriter;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13_1to1_13_2.Protocol1_13_1To1_13_2;
import com.viaversion.viaversion.util.Key;
public class ItemPacketRewriter1_13_2 {
    public static void register(Protocol1_13_1To1_13_2 protocol) {
        protocol.registerClientbound(ClientboundPackets1_13.CONTAINER_SET_SLOT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); 
                map(Types.SHORT); 
                map(Types.ITEM1_13, Types.ITEM1_13_2); 
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.CONTAINER_SET_CONTENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); 
                map(Types.ITEM1_13_SHORT_ARRAY, Types.ITEM1_13_2_SHORT_ARRAY); 
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.CUSTOM_PAYLOAD, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); 
                handlerSoftFail(wrapper -> {
                    String channel = Key.namespaced(wrapper.get(Types.STRING, 0));
                    if (channel.equals("minecraft:trader_list")) {
                        wrapper.passthrough(Types.INT); 
                        int size = wrapper.passthrough(Types.UNSIGNED_BYTE);
                        for (int i = 0; i < size; i++) {
                            wrapper.write(Types.ITEM1_13_2, wrapper.read(Types.ITEM1_13));
                            wrapper.write(Types.ITEM1_13_2, wrapper.read(Types.ITEM1_13));
                            boolean secondItem = wrapper.passthrough(Types.BOOLEAN); 
                            if (secondItem) {
                                wrapper.write(Types.ITEM1_13_2, wrapper.read(Types.ITEM1_13));
                            }
                            wrapper.passthrough(Types.BOOLEAN); 
                            wrapper.passthrough(Types.INT); 
                            wrapper.passthrough(Types.INT); 
                        }
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.SET_EQUIPPED_ITEM, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.VAR_INT); 
                map(Types.ITEM1_13, Types.ITEM1_13_2); 
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.UPDATE_RECIPES, wrapper -> {
            int recipesNo = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < recipesNo; i++) {
                wrapper.passthrough(Types.STRING); 
                String type = wrapper.passthrough(Types.STRING);
                if (type.equals("crafting_shapeless")) {
                    wrapper.passthrough(Types.STRING); 
                    int ingredientsNo = wrapper.passthrough(Types.VAR_INT);
                    for (int i1 = 0; i1 < ingredientsNo; i1++) {
                        wrapper.write(Types.ITEM1_13_2_ARRAY, wrapper.read(Types.ITEM1_13_ARRAY));
                    }
                    wrapper.write(Types.ITEM1_13_2, wrapper.read(Types.ITEM1_13));
                } else if (type.equals("crafting_shaped")) {
                    int ingredientsNo = wrapper.passthrough(Types.VAR_INT) * wrapper.passthrough(Types.VAR_INT);
                    wrapper.passthrough(Types.STRING); 
                    for (int i1 = 0; i1 < ingredientsNo; i1++) {
                        wrapper.write(Types.ITEM1_13_2_ARRAY, wrapper.read(Types.ITEM1_13_ARRAY));
                    }
                    wrapper.write(Types.ITEM1_13_2, wrapper.read(Types.ITEM1_13));
                } else if (type.equals("smelting")) {
                    wrapper.passthrough(Types.STRING); 
                    wrapper.write(Types.ITEM1_13_2_ARRAY, wrapper.read(Types.ITEM1_13_ARRAY));
                    wrapper.write(Types.ITEM1_13_2, wrapper.read(Types.ITEM1_13));
                    wrapper.passthrough(Types.FLOAT); 
                    wrapper.passthrough(Types.VAR_INT); 
                }
            }
        });
        protocol.registerServerbound(ServerboundPackets1_13.CONTAINER_CLICK, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); 
                map(Types.SHORT); 
                map(Types.BYTE); 
                map(Types.SHORT); 
                map(Types.VAR_INT); 
                map(Types.ITEM1_13_2, Types.ITEM1_13); 
            }
        });
        protocol.registerServerbound(ServerboundPackets1_13.SET_CREATIVE_MODE_SLOT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.SHORT); 
                map(Types.ITEM1_13_2, Types.ITEM1_13); 
            }
        });
    }
}

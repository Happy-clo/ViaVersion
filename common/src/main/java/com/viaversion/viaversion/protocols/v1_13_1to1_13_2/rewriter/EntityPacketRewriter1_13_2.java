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
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_13;
import com.viaversion.viaversion.api.type.types.version.Types1_13_2;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13_1to1_13_2.Protocol1_13_1To1_13_2;
public class EntityPacketRewriter1_13_2 {
    public static void register(Protocol1_13_1To1_13_2 protocol) {
        final PacketHandler dataTypeHandler = wrapper -> {
            for (EntityData data : wrapper.get(Types1_13_2.ENTITY_DATA_LIST, 0)) {
                data.setDataType(Types1_13_2.ENTITY_DATA_TYPES.byId(data.dataType().typeId()));
            }
        };
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
                map(Types1_13.ENTITY_DATA_LIST, Types1_13_2.ENTITY_DATA_LIST); 
                handler(dataTypeHandler);
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
                map(Types1_13.ENTITY_DATA_LIST, Types1_13_2.ENTITY_DATA_LIST); 
                handler(dataTypeHandler);
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.SET_ENTITY_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types1_13.ENTITY_DATA_LIST, Types1_13_2.ENTITY_DATA_LIST); 
                handler(dataTypeHandler);
            }
        });
    }
}

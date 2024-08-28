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
package com.viaversion.viaversion.protocols.v1_9to1_9_1;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;
public class Protocol1_9To1_9_1 extends AbstractProtocol<ClientboundPackets1_9, ClientboundPackets1_9, ServerboundPackets1_9, ServerboundPackets1_9> {
    public Protocol1_9To1_9_1() {
        super(ClientboundPackets1_9.class, ClientboundPackets1_9.class, ServerboundPackets1_9.class, ServerboundPackets1_9.class);
    }
    @Override
    protected void registerPackets() {
        registerClientbound(ClientboundPackets1_9.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); 
                map(Types.UNSIGNED_BYTE); 
                map(Types.BYTE, Types.INT); 
                map(Types.UNSIGNED_BYTE); 
                map(Types.UNSIGNED_BYTE); 
                map(Types.STRING); 
                map(Types.BOOLEAN); 
            }
        });
        registerClientbound(ClientboundPackets1_9.SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                handler(wrapper -> {
                    int sound = wrapper.get(Types.VAR_INT, 0);
                    if (sound >= 415) 
                        wrapper.set(Types.VAR_INT, 0, sound + 1);
                });
            }
        });
    }
}

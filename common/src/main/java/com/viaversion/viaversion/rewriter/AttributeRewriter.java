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
package com.viaversion.viaversion.rewriter;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.type.Types;
public class AttributeRewriter<C extends ClientboundPacketType> {
    private final Protocol<C, ?, ?, ?> protocol;
    public AttributeRewriter(Protocol<C, ?, ?, ?> protocol) {
        this.protocol = protocol;
    }
    public void register1_21(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); 
            final int size = wrapper.passthrough(Types.VAR_INT);
            int newSize = size;
            for (int i = 0; i < size; i++) {
                final int attributeId = wrapper.read(Types.VAR_INT);
                final int mappedId = protocol.getMappingData().getNewAttributeId(attributeId);
                if (mappedId == -1) {
                    newSize--;
                    wrapper.read(Types.DOUBLE); 
                    final int modifierSize = wrapper.read(Types.VAR_INT);
                    for (int j = 0; j < modifierSize; j++) {
                        wrapper.read(Types.STRING); 
                        wrapper.read(Types.DOUBLE); 
                        wrapper.read(Types.BYTE); 
                    }
                    continue;
                }
                wrapper.write(Types.VAR_INT, mappedId);
                wrapper.passthrough(Types.DOUBLE); 
                final int modifierSize = wrapper.passthrough(Types.VAR_INT);
                for (int j = 0; j < modifierSize; j++) {
                    wrapper.passthrough(Types.STRING); 
                    wrapper.passthrough(Types.DOUBLE); 
                    wrapper.passthrough(Types.BYTE); 
                }
            }
            if (size != newSize) {
                wrapper.set(Types.VAR_INT, 1, newSize);
            }
        });
    }
}

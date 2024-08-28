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
import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
/**
 * Abstract rewriter for the declare commands packet to handle argument type name and content changes.
 */
public class CommandRewriter<C extends ClientboundPacketType> {
    protected final Protocol<C, ?, ?, ?> protocol;
    protected final Map<String, CommandArgumentConsumer> parserHandlers = new HashMap<>();
    public CommandRewriter(Protocol<C, ?, ?, ?> protocol) {
        this.protocol = protocol;
        this.parserHandlers.put("brigadier:double", wrapper -> {
            byte propertyFlags = wrapper.passthrough(Types.BYTE); 
            if ((propertyFlags & 0x01) != 0) wrapper.passthrough(Types.DOUBLE); 
            if ((propertyFlags & 0x02) != 0) wrapper.passthrough(Types.DOUBLE); 
        });
        this.parserHandlers.put("brigadier:float", wrapper -> {
            byte propertyFlags = wrapper.passthrough(Types.BYTE); 
            if ((propertyFlags & 0x01) != 0) wrapper.passthrough(Types.FLOAT); 
            if ((propertyFlags & 0x02) != 0) wrapper.passthrough(Types.FLOAT); 
        });
        this.parserHandlers.put("brigadier:integer", wrapper -> {
            byte propertyFlags = wrapper.passthrough(Types.BYTE); 
            if ((propertyFlags & 0x01) != 0) wrapper.passthrough(Types.INT); 
            if ((propertyFlags & 0x02) != 0) wrapper.passthrough(Types.INT); 
        });
        this.parserHandlers.put("brigadier:long", wrapper -> {
            byte propertyFlags = wrapper.passthrough(Types.BYTE); 
            if ((propertyFlags & 0x01) != 0) wrapper.passthrough(Types.LONG); 
            if ((propertyFlags & 0x02) != 0) wrapper.passthrough(Types.LONG); 
        });
        this.parserHandlers.put("brigadier:string", wrapper -> wrapper.passthrough(Types.VAR_INT)); 
        this.parserHandlers.put("minecraft:entity", wrapper -> wrapper.passthrough(Types.BYTE)); 
        this.parserHandlers.put("minecraft:score_holder", wrapper -> wrapper.passthrough(Types.BYTE)); 
        this.parserHandlers.put("minecraft:resource", wrapper -> wrapper.passthrough(Types.STRING)); 
        this.parserHandlers.put("minecraft:resource_or_tag", wrapper -> wrapper.passthrough(Types.STRING)); 
        this.parserHandlers.put("minecraft:resource_or_tag_key", wrapper -> wrapper.passthrough(Types.STRING)); 
        this.parserHandlers.put("minecraft:resource_key", wrapper -> wrapper.passthrough(Types.STRING)); 
    }
    public void registerDeclareCommands(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                byte flags = wrapper.passthrough(Types.BYTE);
                wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE); 
                if ((flags & 0x08) != 0) {
                    wrapper.passthrough(Types.VAR_INT); 
                }
                byte nodeType = (byte) (flags & 0x03);
                if (nodeType == 1 || nodeType == 2) { 
                    wrapper.passthrough(Types.STRING); 
                }
                if (nodeType == 2) { 
                    String argumentType = wrapper.read(Types.STRING);
                    String newArgumentType = handleArgumentType(argumentType);
                    if (newArgumentType != null) {
                        wrapper.write(Types.STRING, newArgumentType);
                    }
                    handleArgument(wrapper, argumentType);
                }
                if ((flags & 0x10) != 0) {
                    wrapper.passthrough(Types.STRING); 
                }
            }
            wrapper.passthrough(Types.VAR_INT); 
        });
    }
    public void registerDeclareCommands1_19(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                byte flags = wrapper.passthrough(Types.BYTE);
                wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE); 
                if ((flags & 0x08) != 0) {
                    wrapper.passthrough(Types.VAR_INT); 
                }
                byte nodeType = (byte) (flags & 0x03);
                if (nodeType == 1 || nodeType == 2) { 
                    wrapper.passthrough(Types.STRING); 
                }
                if (nodeType == 2) { 
                    int argumentTypeId = wrapper.read(Types.VAR_INT);
                    String argumentType = argumentType(argumentTypeId);
                    if (argumentType == null) {
                        wrapper.write(Types.VAR_INT, mapInvalidArgumentType(argumentTypeId));
                        continue;
                    }
                    String newArgumentType = handleArgumentType(argumentType);
                    Preconditions.checkNotNull(newArgumentType, "No mapping for argument type %s", argumentType);
                    wrapper.write(Types.VAR_INT, mappedArgumentTypeId(newArgumentType));
                    handleArgument(wrapper, argumentType);
                    if ((flags & 0x10) != 0) {
                        wrapper.passthrough(Types.STRING); 
                    }
                }
            }
            wrapper.passthrough(Types.VAR_INT); 
        });
    }
    public void handleArgument(PacketWrapper wrapper, String argumentType) {
        CommandArgumentConsumer handler = parserHandlers.get(argumentType);
        if (handler != null) {
            handler.accept(wrapper);
        }
    }
    /**
     * Can be overridden if needed.
     *
     * @param argumentType argument type
     * @return mapped argument type
     */
    public String handleArgumentType(final String argumentType) {
        if (protocol.getMappingData() != null && protocol.getMappingData().getArgumentTypeMappings() != null) {
            return protocol.getMappingData().getArgumentTypeMappings().mappedIdentifier(argumentType);
        }
        return argumentType;
    }
    protected @Nullable String argumentType(final int argumentTypeId) {
        final FullMappings mappings = protocol.getMappingData().getArgumentTypeMappings();
        final String identifier = mappings.identifier(argumentTypeId);
        Preconditions.checkArgument(identifier != null || argumentTypeId >= mappings.size(), "Unknown argument type id %s", argumentTypeId);
        return identifier;
    }
    protected int mappedArgumentTypeId(final String mappedArgumentType) {
        return protocol.getMappingData().getArgumentTypeMappings().mappedId(mappedArgumentType);
    }
    private int mapInvalidArgumentType(final int id) {
        if (id < 0) {
            return id;
        }
        final FullMappings mappings = protocol.getMappingData().getArgumentTypeMappings();
        final int idx = id - mappings.size();
        return mappings.mappedSize() + idx;
    }
    @FunctionalInterface
    public interface CommandArgumentConsumer {
        void accept(PacketWrapper wrapper);
    }
}

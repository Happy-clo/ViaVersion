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
package com.viaversion.viaversion.protocols.v1_20to1_20_2.packet;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPacket1_20_3;
public enum ServerboundConfigurationPackets1_20_2 implements ServerboundPacket1_20_2, ServerboundPacket1_20_3 {
    CLIENT_INFORMATION, 
    CUSTOM_PAYLOAD, 
    FINISH_CONFIGURATION, 
    KEEP_ALIVE, 
    PONG, 
    RESOURCE_PACK; 
    @Override
    public int getId() {
        return ordinal();
    }
    @Override
    public String getName() {
        return name();
    }
    @Override
    public State state() {
        return State.CONFIGURATION;
    }
}

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
package com.viaversion.viaversion.protocols.v1_20_5to1_21.packet;
import com.viaversion.viaversion.api.protocol.packet.State;
public enum ClientboundConfigurationPackets1_21 implements ClientboundPacket1_21 {
    COOKIE_REQUEST, 
    CUSTOM_PAYLOAD, 
    DISCONNECT, 
    FINISH_CONFIGURATION, 
    KEEP_ALIVE, 
    PING, 
    RESET_CHAT, 
    REGISTRY_DATA, 
    RESOURCE_PACK_POP, 
    RESOURCE_PACK_PUSH, 
    STORE_COOKIE, 
    TRANSFER, 
    UPDATE_ENABLED_FEATURES, 
    UPDATE_TAGS, 
    SELECT_KNOWN_PACKS, 
    CUSTOM_REPORT_DETAILS, 
    SERVER_LINKS; 
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

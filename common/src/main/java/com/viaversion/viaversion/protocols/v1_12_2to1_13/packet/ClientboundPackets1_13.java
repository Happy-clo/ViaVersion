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
package com.viaversion.viaversion.protocols.v1_12_2to1_13.packet;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
public enum ClientboundPackets1_13 implements ClientboundPacketType {
    ADD_ENTITY, 
    ADD_EXPERIENCE_ORB, 
    ADD_GLOBAL_ENTITY, 
    ADD_MOB, 
    ADD_PAINTING, 
    ADD_PLAYER, 
    ANIMATE, 
    AWARD_STATS, 
    BLOCK_DESTRUCTION, 
    BLOCK_ENTITY_DATA, 
    BLOCK_EVENT, 
    BLOCK_UPDATE, 
    BOSS_EVENT, 
    CHANGE_DIFFICULTY, 
    CHAT, 
    CHUNK_BLOCKS_UPDATE, 
    COMMAND_SUGGESTIONS, 
    COMMANDS, 
    CONTAINER_ACK, 
    CONTAINER_CLOSE, 
    OPEN_SCREEN, 
    CONTAINER_SET_CONTENT, 
    CONTAINER_SET_DATA, 
    CONTAINER_SET_SLOT, 
    COOLDOWN, 
    CUSTOM_PAYLOAD, 
    CUSTOM_SOUND, 
    DISCONNECT, 
    ENTITY_EVENT, 
    TAG_QUERY, 
    EXPLODE, 
    FORGET_LEVEL_CHUNK, 
    GAME_EVENT, 
    KEEP_ALIVE, 
    LEVEL_CHUNK, 
    LEVEL_EVENT, 
    LEVEL_PARTICLES, 
    LOGIN, 
    MAP_ITEM_DATA, 
    MOVE_ENTITY, 
    MOVE_ENTITY_POS, 
    MOVE_ENTITY_POS_ROT, 
    MOVE_ENTITY_ROT, 
    MOVE_VEHICLE, 
    OPEN_SIGN_EDITOR, 
    PLACE_GHOST_RECIPE, 
    PLAYER_ABILITIES, 
    PLAYER_COMBAT, 
    PLAYER_INFO, 
    PLAYER_LOOK_AT, 
    PLAYER_POSITION, 
    PLAYER_SLEEP, 
    RECIPE, 
    REMOVE_ENTITIES, 
    REMOVE_MOB_EFFECT, 
    RESOURCE_PACK, 
    RESPAWN, 
    ROTATE_HEAD, 
    SELECT_ADVANCEMENTS_TAB, 
    SET_BORDER, 
    SET_CAMERA, 
    SET_CARRIED_ITEM, 
    SET_DISPLAY_OBJECTIVE, 
    SET_ENTITY_DATA, 
    SET_ENTITY_LINK, 
    SET_ENTITY_MOTION, 
    SET_EQUIPPED_ITEM, 
    SET_EXPERIENCE, 
    SET_HEALTH, 
    SET_OBJECTIVE, 
    SET_PASSENGERS, 
    SET_PLAYER_TEAM, 
    SET_SCORE, 
    SET_DEFAULT_SPAWN_POSITION, 
    SET_TIME, 
    SET_TITLES, 
    STOP_SOUND, 
    SOUND, 
    TAB_LIST, 
    TAKE_ITEM_ENTITY, 
    TELEPORT_ENTITY, 
    UPDATE_ADVANCEMENTS, 
    UPDATE_ATTRIBUTES, 
    UPDATE_MOB_EFFECT, 
    UPDATE_RECIPES, 
    UPDATE_TAGS; 
    @Override
    public int getId() {
        return ordinal();
    }
    @Override
    public String getName() {
        return name();
    }
}

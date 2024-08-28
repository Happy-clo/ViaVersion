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
package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.data;
import com.viaversion.viaversion.util.KeyMappings;
import org.checkerframework.checker.nullness.qual.Nullable;
public final class EquipmentSlots1_20_5 {
    public static final KeyMappings SLOTS = new KeyMappings(
        "any",
        "mainhand",
        "offhand",
        "hand",
        "feet",
        "legs",
        "chest",
        "head",
        "armor",
        "body"
    );
    public static @Nullable String idToKey(final int id) {
        return SLOTS.idToKey(id);
    }
    public static int keyToId(final String enchantment) {
        return SLOTS.keyToId(enchantment);
    }
}

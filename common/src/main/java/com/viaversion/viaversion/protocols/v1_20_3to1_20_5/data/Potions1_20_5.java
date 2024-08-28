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
public final class Potions1_20_5 {
    private static final KeyMappings POTIONS = new KeyMappings(
        "water",
        "mundane",
        "thick",
        "awkward",
        "night_vision",
        "long_night_vision",
        "invisibility",
        "long_invisibility",
        "leaping",
        "long_leaping",
        "strong_leaping",
        "fire_resistance",
        "long_fire_resistance",
        "swiftness",
        "long_swiftness",
        "strong_swiftness",
        "slowness",
        "long_slowness",
        "strong_slowness",
        "turtle_master",
        "long_turtle_master",
        "strong_turtle_master",
        "water_breathing",
        "long_water_breathing",
        "healing",
        "strong_healing",
        "harming",
        "strong_harming",
        "poison",
        "long_poison",
        "strong_poison",
        "regeneration",
        "long_regeneration",
        "strong_regeneration",
        "strength",
        "long_strength",
        "strong_strength",
        "weakness",
        "long_weakness",
        "luck",
        "slow_falling",
        "long_slow_falling",
        "wind_charged",
        "weaving",
        "oozing",
        "infested"
    );
    public static @Nullable String idToKey(final int id) {
        return POTIONS.idToKey(id);
    }
    public static int keyToId(final String potion) {
        return POTIONS.keyToId(potion);
    }
}

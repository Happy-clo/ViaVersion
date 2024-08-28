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
package com.viaversion.viaversion.protocols.v1_8to1_9.data;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
public class EffectIdMappings1_9 {
    private static final Int2IntMap EFFECTS = new Int2IntOpenHashMap(19, .99F);
    static {
        addRewrite(1005, 1010); 
        addRewrite(1003, 1005); 
        addRewrite(1006, 1011); 
        addRewrite(1004, 1009); 
        addRewrite(1007, 1015); 
        addRewrite(1008, 1016); 
        addRewrite(1009, 1016); 
        addRewrite(1010, 1019); 
        addRewrite(1011, 1020); 
        addRewrite(1012, 1021); 
        addRewrite(1014, 1024); 
        addRewrite(1015, 1025); 
        addRewrite(1016, 1026); 
        addRewrite(1017, 1027); 
        addRewrite(1020, 1029); 
        addRewrite(1021, 1030); 
        addRewrite(1022, 1031); 
        addRewrite(1013, 1023); 
        addRewrite(1018, 1028); 
    }
    public static int getNewId(int id) {
        return EFFECTS.getOrDefault(id, id);
    }
    public static boolean contains(int oldId) {
        return EFFECTS.containsKey(oldId);
    }
    private static void addRewrite(int oldId, int newId) {
        EFFECTS.put(oldId, newId);
    }
}

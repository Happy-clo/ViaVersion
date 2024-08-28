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
package com.viaversion.viaversion.protocols.v1_17_1to1_18.data;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.Protocol1_17_1To1_18;
import java.util.Arrays;
public final class BlockEntityMappings1_18 {
    private static final int[] IDS = new int[14];
    static {
        Arrays.fill(IDS, -1);
        IDS[1] = 8; 
        IDS[2] = 21; 
        IDS[3] = 13; 
        IDS[4] = 14; 
        IDS[5] = 24; 
        IDS[6] = 18; 
        IDS[7] = 19; 
        IDS[8] = 20; 
        IDS[9] = 7; 
        IDS[10] = 22; 
        IDS[11] = 23; 
        IDS[12] = 30; 
        IDS[13] = 31; 
    }
    public static int newId(final int id) {
        final int newId;
        if (id < 0 || id > IDS.length || (newId = IDS[id]) == -1) {
            Protocol1_17_1To1_18.LOGGER.warning("Received out of bounds block entity id: " + id);
            return -1;
        }
        return newId;
    }
    public static int[] getIds() {
        return IDS;
    }
}

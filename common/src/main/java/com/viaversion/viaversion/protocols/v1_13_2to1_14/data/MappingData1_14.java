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
package com.viaversion.viaversion.protocols.v1_13_2to1_14.data;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
public class MappingData1_14 extends MappingDataBase {
    private IntSet motionBlocking;
    private IntSet nonFullBlocks;
    public MappingData1_14() {
        super("1.13.2", "1.14");
    }
    @Override
    public void loadExtras(final CompoundTag data) {
        final CompoundTag heightmap = MappingDataLoader.INSTANCE.loadNBT("heightmap-1.14.nbt");
        final IntArrayTag motionBlocking = heightmap.getIntArrayTag("motionBlocking");
        this.motionBlocking = new IntOpenHashSet(motionBlocking.getValue());
        if (Via.getConfig().isNonFullBlockLightFix()) {
            final IntArrayTag nonFullBlocks = heightmap.getIntArrayTag("nonFullBlocks");
            this.nonFullBlocks = new IntOpenHashSet(nonFullBlocks.getValue());
        }
    }
    public IntSet getMotionBlocking() {
        return motionBlocking;
    }
    public IntSet getNonFullBlocks() {
        return nonFullBlocks;
    }
}

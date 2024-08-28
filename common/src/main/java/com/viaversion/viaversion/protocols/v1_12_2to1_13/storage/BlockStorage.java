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
package com.viaversion.viaversion.protocols.v1_12_2to1_13.storage;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.HashMap;
import java.util.Map;
public class BlockStorage implements StorableObject {
    private static final IntSet WHITELIST = new IntOpenHashSet(46, .99F);
    private final Map<BlockPosition, ReplacementData> blocks = new HashMap<>();
    static {
        WHITELIST.add(5266);
        for (int i = 0; i < 16; i++) {
            WHITELIST.add(972 + i);
        }
        for (int i = 0; i < 20; i++) {
            WHITELIST.add(6854 + i);
        }
        for (int i = 0; i < 4; i++) {
            WHITELIST.add(7110 + i);
        }
        for (int i = 0; i < 5; i++) {
            WHITELIST.add(5447 + i);
        }
    }
    public void store(BlockPosition position, int block) {
        store(position, block, -1);
    }
    public void store(BlockPosition position, int block, int replacementId) {
        if (!WHITELIST.contains(block))
            return;
        blocks.put(position, new ReplacementData(block, replacementId));
    }
    public boolean isWelcome(int block) {
        return WHITELIST.contains(block);
    }
    public boolean contains(BlockPosition position) {
        return blocks.containsKey(position);
    }
    public ReplacementData get(BlockPosition position) {
        return blocks.get(position);
    }
    public ReplacementData remove(BlockPosition position) {
        return blocks.remove(position);
    }
    public static final class ReplacementData {
        private final int original;
        private int replacement;
        public ReplacementData(int original, int replacement) {
            this.original = original;
            this.replacement = replacement;
        }
        public int getOriginal() {
            return original;
        }
        public int getReplacement() {
            return replacement;
        }
        public void setReplacement(int replacement) {
            this.replacement = replacement;
        }
    }
}

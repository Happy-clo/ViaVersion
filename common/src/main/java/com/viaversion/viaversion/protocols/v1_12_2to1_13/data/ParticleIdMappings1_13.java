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
package com.viaversion.viaversion.protocols.v1_12_2to1_13.data;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.rewriter.WorldPacketRewriter1_13;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.checkerframework.checker.nullness.qual.Nullable;
public class ParticleIdMappings1_13 {
    private static final List<NewParticle> particles = new ArrayList<>();
    static {
        add(34); 
        add(19); 
        add(18); 
        add(21); 
        add(4); 
        add(43); 
        add(22); 
        add(42); 
        add(32); 
        add(6); 
        add(14); 
        add(37); 
        add(30); 
        add(12); 
        add(26); 
        add(17); 
        add(0); 
        add(44); 
        add(10); 
        add(9); 
        add(1); 
        add(24); 
        add(32); 
        add(33); 
        add(35); 
        add(15); 
        add(23); 
        add(31); 
        add(-1); 
        add(5); 
        add(11, reddustHandler()); 
        add(29); 
        add(34); 
        add(28); 
        add(25); 
        add(2); 
        add(27, iconcrackHandler()); 
        add(3, blockHandler()); 
        add(3, blockHandler()); 
        add(36); 
        add(-1); 
        add(13); 
        add(8); 
        add(16); 
        add(7); 
        add(40); 
        add(20, blockHandler()); 
        add(41); 
        add(38); 
    }
    public static Particle rewriteParticle(int particleId, Integer[] data) {
        if (particleId >= particles.size()) {
            Protocol1_12_2To1_13.LOGGER.severe("Failed to transform particles with id " + particleId + " and data " + Arrays.toString(data));
            return null;
        }
        NewParticle rewrite = particles.get(particleId);
        return rewrite.handle(new Particle(rewrite.id()), data);
    }
    private static void add(int newId) {
        particles.add(new NewParticle(newId, null));
    }
    private static void add(int newId, ParticleDataHandler dataHandler) {
        particles.add(new NewParticle(newId, dataHandler));
    }
    private static ParticleDataHandler reddustHandler() {
        return (particle, data) -> {
            particle.add(Types.FLOAT, randomBool() ? 1f : 0f); 
            particle.add(Types.FLOAT, 0f); 
            particle.add(Types.FLOAT, randomBool() ? 1f : 0f); 
            particle.add(Types.FLOAT, 1f);
            return particle;
        };
    }
    private static boolean randomBool() {
        return ThreadLocalRandom.current().nextBoolean();
    }
    private static ParticleDataHandler iconcrackHandler() {
        return (particle, data) -> {
            Item item;
            if (data.length == 1) {
                item = new DataItem(data[0], (byte) 1, (short) 0, null);
            } else if (data.length == 2) {
                item = new DataItem(data[0], (byte) 1, data[1].shortValue(), null);
            } else {
                return particle;
            }
            Via.getManager().getProtocolManager().getProtocol(Protocol1_12_2To1_13.class).getItemRewriter().handleItemToClient(null, item);
            particle.add(Types.ITEM1_13, item); 
            return particle;
        };
    }
    private static ParticleDataHandler blockHandler() {
        return (particle, data) -> {
            int value = data[0];
            int combined = (((value & 4095) << 4) | (value >> 12 & 15));
            int newId = WorldPacketRewriter1_13.toNewId(combined);
            particle.add(Types.VAR_INT, newId); 
            return particle;
        };
    }
    @FunctionalInterface
    interface ParticleDataHandler {
        Particle handler(Particle particle, Integer[] data);
    }
    private record NewParticle(int id, @Nullable ParticleDataHandler handler) {
        public Particle handle(Particle particle, Integer[] data) {
            if (handler != null) {
                return handler.handler(particle, data);
            }
            return particle;
        }
    }
}

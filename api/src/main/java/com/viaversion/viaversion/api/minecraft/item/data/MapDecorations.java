/*
 * This file is part of ViaVersion - https:
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.minecraft.item.data;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
public record MapDecorations(Map<String, MapDecoration> decorations) {
    public static final Type<MapDecorations> TYPE = new Type<>(MapDecorations.class) {
        @Override
        public MapDecorations read(final ByteBuf buffer) {
            final Object2ObjectMap<String, MapDecoration> decorations = new Object2ObjectOpenHashMap<>();
            final int size = Types.VAR_INT.readPrimitive(buffer);
            for (int i = 0; i < size; i++) {
                final String id = Types.STRING.read(buffer);
                final MapDecoration decoration = MapDecoration.TYPE.read(buffer);
                decorations.put(id, decoration);
            }
            return new MapDecorations(decorations);
        }
        @Override
        public void write(final ByteBuf buffer, final MapDecorations value) {
            Types.VAR_INT.writePrimitive(buffer, value.decorations.size());
            for (final Map.Entry<String, MapDecoration> entry : value.decorations.entrySet()) {
                Types.STRING.write(buffer, entry.getKey());
                MapDecoration.TYPE.write(buffer, entry.getValue());
            }
        }
    };
}

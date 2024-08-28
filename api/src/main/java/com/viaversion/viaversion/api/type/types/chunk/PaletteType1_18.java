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
package com.viaversion.viaversion.api.type.types.chunk;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.DataPaletteImpl;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.CompactArrayUtil;
import com.viaversion.viaversion.util.MathUtil;
import io.netty.buffer.ByteBuf;
public final class PaletteType1_18 extends Type<DataPalette> {
    private final int globalPaletteBits;
    private final PaletteType type;
    public PaletteType1_18(final PaletteType type, final int globalPaletteBits) {
        super(DataPalette.class);
        this.globalPaletteBits = globalPaletteBits;
        this.type = type;
    }
    @Override
    public DataPalette read(final ByteBuf buffer) {
        int bitsPerValue = buffer.readByte();
        final DataPaletteImpl palette;
        if (bitsPerValue == 0) {
            palette = new DataPaletteImpl(type.size(), 1);
            palette.addId(Types.VAR_INT.readPrimitive(buffer));
            Types.LONG_ARRAY_PRIMITIVE.read(buffer); 
            return palette;
        }
        if (bitsPerValue < 0 || bitsPerValue > type.highestBitsPerValue()) {
            bitsPerValue = globalPaletteBits;
        } else if (type == PaletteType.BLOCKS && bitsPerValue < 4) {
            bitsPerValue = 4; 
        }
        if (bitsPerValue != globalPaletteBits) {
            final int paletteLength = Types.VAR_INT.readPrimitive(buffer);
            palette = new DataPaletteImpl(type.size(), paletteLength);
            for (int i = 0; i < paletteLength; i++) {
                palette.addId(Types.VAR_INT.readPrimitive(buffer));
            }
        } else {
            palette = new DataPaletteImpl(type.size());
        }
        final long[] values = Types.LONG_ARRAY_PRIMITIVE.read(buffer);
        if (values.length > 0) {
            final int valuesPerLong = (char) (64 / bitsPerValue);
            final int expectedLength = (type.size() + valuesPerLong - 1) / valuesPerLong;
            if (values.length == expectedLength) { 
                CompactArrayUtil.iterateCompactArrayWithPadding(bitsPerValue, type.size(), values,
                    bitsPerValue == globalPaletteBits ? palette::setIdAt : palette::setPaletteIndexAt);
            }
        }
        return palette;
    }
    @Override
    public void write(final ByteBuf buffer, final DataPalette palette) {
        final int size = palette.size();
        if (size == 1) {
            buffer.writeByte(0); 
            Types.VAR_INT.writePrimitive(buffer, palette.idByIndex(0));
            Types.VAR_INT.writePrimitive(buffer, 0); 
            return;
        }
        final int min = type == PaletteType.BLOCKS ? 4 : 1;
        int bitsPerValue = Math.max(min, MathUtil.ceilLog2(size));
        if (bitsPerValue > type.highestBitsPerValue()) {
            bitsPerValue = globalPaletteBits;
        }
        buffer.writeByte(bitsPerValue);
        if (bitsPerValue != globalPaletteBits) {
            Types.VAR_INT.writePrimitive(buffer, size);
            for (int i = 0; i < size; i++) {
                Types.VAR_INT.writePrimitive(buffer, palette.idByIndex(i));
            }
        }
        Types.LONG_ARRAY_PRIMITIVE.write(buffer, CompactArrayUtil.createCompactArrayWithPadding(bitsPerValue, type.size(), bitsPerValue == globalPaletteBits ? palette::idAt : palette::paletteIndexAt));
    }
}

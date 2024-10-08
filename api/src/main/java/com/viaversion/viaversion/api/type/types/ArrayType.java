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
package com.viaversion.viaversion.api.type.types;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Array;
public class ArrayType<T> extends Type<T[]> {
    private final Type<T> elementType;
    public ArrayType(Type<T> type) {
        super(type.getTypeName() + " Array", (Class<T[]>) getArrayClass(type.getOutputClass()));
        this.elementType = type;
    }
    public static Class<?> getArrayClass(Class<?> componentType) {
        return Array.newInstance(componentType, 0).getClass();
    }
    @Override
    public T[] read(ByteBuf buffer) {
        int amount = Types.VAR_INT.readPrimitive(buffer);
        T[] array = (T[]) Array.newInstance(elementType.getOutputClass(), amount);
        for (int i = 0; i < amount; i++) {
            array[i] = elementType.read(buffer);
        }
        return array;
    }
    @Override
    public void write(ByteBuf buffer, T[] object) {
        Types.VAR_INT.writePrimitive(buffer, object.length);
        for (T o : object) {
            elementType.write(buffer, o);
        }
    }
}

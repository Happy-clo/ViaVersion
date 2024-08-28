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
package com.viaversion.viaversion.api.type.types.misc;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
public class ProfileKeyType extends Type<ProfileKey> {
    public ProfileKeyType() {
        super(ProfileKey.class);
    }
    @Override
    public ProfileKey read(final ByteBuf buffer) {
        return new ProfileKey(buffer.readLong(), Types.BYTE_ARRAY_PRIMITIVE.read(buffer), Types.BYTE_ARRAY_PRIMITIVE.read(buffer));
    }
    @Override
    public void write(final ByteBuf buffer, final ProfileKey object) {
        buffer.writeLong(object.expiresAt());
        Types.BYTE_ARRAY_PRIMITIVE.write(buffer, object.publicKey());
        Types.BYTE_ARRAY_PRIMITIVE.write(buffer, object.keySignature());
    }
    public static final class OptionalProfileKeyType extends OptionalType<ProfileKey> {
        public OptionalProfileKeyType() {
            super(Types.PROFILE_KEY);
        }
    }
}

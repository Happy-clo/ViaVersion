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
package com.viaversion.viaversion.protocols.v1_11_1to1_12;
import com.google.gson.JsonElement;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_12;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_9_3;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.data.ChatItemRewriter;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.data.TranslateRewriter;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.packet.ClientboundPackets1_12;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.packet.ServerboundPackets1_12;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.provider.InventoryQuickMoveProvider;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.rewriter.EntityPacketRewriter1_12;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.rewriter.ItemPacketRewriter1_12;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ServerboundPackets1_9_3;
import com.viaversion.viaversion.rewriter.SoundRewriter;
public class Protocol1_11_1To1_12 extends AbstractProtocol<ClientboundPackets1_9_3, ClientboundPackets1_12, ServerboundPackets1_9_3, ServerboundPackets1_12> {
    private final EntityPacketRewriter1_12 entityRewriter = new EntityPacketRewriter1_12(this);
    private final ItemPacketRewriter1_12 itemRewriter = new ItemPacketRewriter1_12(this);
    public Protocol1_11_1To1_12() {
        super(ClientboundPackets1_9_3.class, ClientboundPackets1_12.class, ServerboundPackets1_9_3.class, ServerboundPackets1_12.class);
    }
    @Override
    protected void registerPackets() {
        super.registerPackets();
        registerClientbound(ClientboundPackets1_9_3.CHAT, wrapper -> {
            if (!Via.getConfig().is1_12NBTArrayFix()) return;
            final JsonElement element = wrapper.passthrough(Types.COMPONENT);
            TranslateRewriter.toClient(wrapper.user(), element);
            ChatItemRewriter.toClient(element);
            wrapper.set(Types.COMPONENT, 0, element);
        });
        registerClientbound(ClientboundPackets1_9_3.LEVEL_CHUNK, wrapper -> {
            ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
            ChunkType1_9_3 type = ChunkType1_9_3.forEnvironment(clientWorld.getEnvironment());
            Chunk chunk = wrapper.passthrough(type);
            for (int s = 0; s < chunk.getSections().length; s++) {
                ChunkSection section = chunk.getSections()[s];
                if (section == null) continue;
                DataPalette blocks = section.palette(PaletteType.BLOCKS);
                for (int idx = 0; idx < ChunkSection.SIZE; idx++) {
                    int id = blocks.idAt(idx) >> 4;
                    if (id != 26) continue;
                    CompoundTag tag = new CompoundTag();
                    tag.put("color", new IntTag(14)); 
                    tag.put("x", new IntTag(ChunkSection.xFromIndex(idx) + (chunk.getX() << 4)));
                    tag.put("y", new IntTag(ChunkSection.yFromIndex(idx) + (s << 4)));
                    tag.put("z", new IntTag(ChunkSection.zFromIndex(idx) + (chunk.getZ() << 4)));
                    tag.put("id", new StringTag("minecraft:bed"));
                    chunk.getBlockEntities().add(tag);
                }
            }
        });
        registerClientbound(ClientboundPackets1_9_3.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT);
                map(Types.UNSIGNED_BYTE);
                map(Types.INT);
                handler(wrapper -> {
                    UserConnection user = wrapper.user();
                    ClientWorld clientChunks = user.get(ClientWorld.class);
                    int dimensionId = wrapper.get(Types.INT, 1);
                    clientChunks.setEnvironment(dimensionId);
                    if (user.getProtocolInfo().protocolVersion().newerThanOrEqualTo(ProtocolVersion.v1_13)) {
                        wrapper.create(ClientboundPackets1_13.UPDATE_RECIPES, packetWrapper -> packetWrapper.write(Types.VAR_INT, 0))
                            .scheduleSend(Protocol1_12_2To1_13.class);
                    }
                });
            }
        });
        registerClientbound(ClientboundPackets1_9_3.RESPAWN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT);
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Types.INT, 0);
                    clientWorld.setEnvironment(dimensionId);
                });
            }
        });
        new SoundRewriter<>(this, this::getNewSoundId).registerSound(ClientboundPackets1_9_3.SOUND);
        cancelServerbound(ServerboundPackets1_12.CRAFTING_RECIPE_PLACEMENT);
        registerServerbound(ServerboundPackets1_12.CLIENT_INFORMATION, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); 
                map(Types.BYTE); 
                map(Types.VAR_INT); 
                map(Types.BOOLEAN); 
                map(Types.UNSIGNED_BYTE); 
                map(Types.VAR_INT); 
                handler(wrapper -> {
                    String locale = wrapper.get(Types.STRING, 0);
                    if (locale.length() > 7) {
                        wrapper.set(Types.STRING, 0, locale.substring(0, 7));
                    }
                });
            }
        });
        cancelServerbound(ServerboundPackets1_12.RECIPE_BOOK_UPDATE);
        cancelServerbound(ServerboundPackets1_12.SEEN_ADVANCEMENTS);
    }
    private int getNewSoundId(int id) {
        int newId = id;
        if (id >= 26) 
            newId += 2;
        if (id >= 70) 
            newId += 4;
        if (id >= 74) 
            newId += 1;
        if (id >= 143) 
            newId += 3;
        if (id >= 185) 
            newId += 1;
        if (id >= 263) 
            newId += 7;
        if (id >= 301) 
            newId += 33;
        if (id >= 317) 
            newId += 2;
        if (id >= 491) 
            newId += 3;
        return newId;
    }
    @Override
    public void register(ViaProviders providers) {
        providers.register(InventoryQuickMoveProvider.class, new InventoryQuickMoveProvider());
    }
    @Override
    public void init(UserConnection userConnection) {
        userConnection.addEntityTracker(this.getClass(), new EntityTrackerBase(userConnection, EntityTypes1_12.EntityType.PLAYER));
        if (!userConnection.has(ClientWorld.class)) {
            userConnection.put(new ClientWorld());
        }
    }
    @Override
    public EntityPacketRewriter1_12 getEntityRewriter() {
        return entityRewriter;
    }
    @Override
    public ItemPacketRewriter1_12 getItemRewriter() {
        return itemRewriter;
    }
}

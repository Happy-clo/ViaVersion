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
package com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundPacket1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.rewriter.RecipeRewriter1_20_2;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.Protocol1_20_2To1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPacket1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.SerializerVersion;
import org.checkerframework.checker.nullness.qual.Nullable;
public final class BlockItemPacketRewriter1_20_3 extends ItemRewriter<ClientboundPacket1_20_2, ServerboundPacket1_20_3, Protocol1_20_2To1_20_3> {
    public BlockItemPacketRewriter1_20_3(final Protocol1_20_2To1_20_3 protocol) {
        super(protocol, Types.ITEM1_20_2, Types.ITEM1_20_2_ARRAY);
    }
    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPacket1_20_2> blockRewriter = BlockRewriter.for1_20_2(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_20_2.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_20_2.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate1_20(ClientboundPackets1_20_2.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerLevelEvent(ClientboundPackets1_20_2.LEVEL_EVENT, 1010, 2001);
        blockRewriter.registerLevelChunk1_19(ClientboundPackets1_20_2.LEVEL_CHUNK_WITH_LIGHT, ChunkType1_20_2::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_20_2.BLOCK_ENTITY_DATA);
        registerCooldown(ClientboundPackets1_20_2.COOLDOWN);
        registerSetContent1_17_1(ClientboundPackets1_20_2.CONTAINER_SET_CONTENT);
        registerSetSlot1_17_1(ClientboundPackets1_20_2.CONTAINER_SET_SLOT);
        registerSetEquipment(ClientboundPackets1_20_2.SET_EQUIPMENT);
        registerContainerClick1_17_1(ServerboundPackets1_20_3.CONTAINER_CLICK);
        registerMerchantOffers1_19(ClientboundPackets1_20_2.MERCHANT_OFFERS);
        registerSetCreativeModeSlot(ServerboundPackets1_20_3.SET_CREATIVE_MODE_SLOT);
        registerContainerSetData(ClientboundPackets1_20_2.CONTAINER_SET_DATA);
        protocol.registerClientbound(ClientboundPackets1_20_2.LEVEL_PARTICLES, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.BOOLEAN); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.FLOAT); 
                map(Types.FLOAT); 
                map(Types.FLOAT); 
                map(Types.FLOAT); 
                map(Types.INT); 
                handler(wrapper -> {
                    final int id = wrapper.get(Types.VAR_INT, 0);
                    final ParticleMappings particleMappings = protocol.getMappingData().getParticleMappings();
                    if (id == particleMappings.id("vibration")) {
                        final String resourceLocation = Key.stripMinecraftNamespace(wrapper.read(Types.STRING));
                        wrapper.write(Types.VAR_INT, resourceLocation.equals("block") ? 0 : 1);
                    }
                });
                handler(levelParticlesHandler(Types.VAR_INT));
            }
        });
        new RecipeRewriter1_20_2<>(protocol) {
            @Override
            public void handleCraftingShaped(final PacketWrapper wrapper) {
                final int width = wrapper.read(Types.VAR_INT);
                final int height = wrapper.read(Types.VAR_INT);
                wrapper.passthrough(Types.STRING); 
                wrapper.passthrough(Types.VAR_INT); 
                wrapper.write(Types.VAR_INT, width);
                wrapper.write(Types.VAR_INT, height);
                final int ingredients = height * width;
                for (int i = 0; i < ingredients; i++) {
                    handleIngredient(wrapper);
                }
                rewrite(wrapper.user(), wrapper.passthrough(itemType())); 
                wrapper.passthrough(Types.BOOLEAN); 
            }
        }.register(ClientboundPackets1_20_2.UPDATE_RECIPES);
        protocol.registerClientbound(ClientboundPackets1_20_2.EXPLODE, wrapper -> {
            wrapper.passthrough(Types.DOUBLE); 
            wrapper.passthrough(Types.DOUBLE); 
            wrapper.passthrough(Types.DOUBLE); 
            wrapper.passthrough(Types.FLOAT); 
            final int blocks = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < blocks; i++) {
                wrapper.passthrough(Types.BYTE); 
                wrapper.passthrough(Types.BYTE); 
                wrapper.passthrough(Types.BYTE); 
            }
            wrapper.passthrough(Types.FLOAT); 
            wrapper.passthrough(Types.FLOAT); 
            wrapper.passthrough(Types.FLOAT); 
            wrapper.write(Types.VAR_INT, 1); 
            wrapper.write(Types1_20_3.PARTICLE, new Particle(protocol.getMappingData().getParticleMappings().mappedId("explosion"))); 
            wrapper.write(Types1_20_3.PARTICLE, new Particle(protocol.getMappingData().getParticleMappings().mappedId("explosion_emitter"))); 
            wrapper.write(Types.STRING, "minecraft:entity.generic.explode"); 
            wrapper.write(Types.OPTIONAL_FLOAT, null); 
        });
    }
    @Override
    public @Nullable Item handleItemToClient(final UserConnection connection, @Nullable final Item item) {
        if (item == null) {
            return null;
        }
        final CompoundTag tag = item.tag();
        if (tag != null && item.identifier() == 1047) { 
            final ListTag<StringTag> pages = tag.getListTag("pages", StringTag.class);
            if (pages != null) {
                for (final StringTag pageTag : pages) {
                    updatePageTag(pageTag);
                }
            }
            final CompoundTag filteredPages = tag.getCompoundTag("filtered_pages");
            if (filteredPages != null) {
                for (final String string : filteredPages.keySet()) {
                    updatePageTag(filteredPages.getStringTag(string));
                }
            }
        }
        return super.handleItemToClient(connection, item);
    }
    private void updatePageTag(final StringTag pageTag) {
        try {
            final JsonElement updatedComponent = ComponentUtil.convertJson(pageTag.getValue(), SerializerVersion.V1_19_4, SerializerVersion.V1_20_3);
            pageTag.setValue(updatedComponent.toString());
        } catch (final Exception e) {
            Via.getManager().debugHandler().error("Error during book conversion", e);
        }
    }
}
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
package com.viaversion.viaversion.protocols.template;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_20_5;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.provider.PacketTypesProvider;
import com.viaversion.viaversion.api.protocol.packet.provider.SimplePacketTypesProvider;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundConfigurationPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.rewriter.AttributeRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import static com.viaversion.viaversion.util.ProtocolUtil.packetTypeMap;
final class Protocol1_99To_98 extends AbstractProtocol<ClientboundPacket1_21, ClientboundPacket1_21, ServerboundPacket1_20_5, ServerboundPacket1_20_5> {
    public static final MappingData MAPPINGS = new MappingDataBase("1.98", "1.99");
    private final EntityPacketRewriter1_99 entityRewriter = new EntityPacketRewriter1_99(this);
    private final BlockItemPacketRewriter1_99 itemRewriter = new BlockItemPacketRewriter1_99(this);
    private final TagRewriter<ClientboundPacket1_21> tagRewriter = new TagRewriter<>(this);
    public Protocol1_99To_98() {
        super(ClientboundPacket1_21.class, ClientboundPacket1_21.class, ServerboundPacket1_20_5.class, ServerboundPacket1_20_5.class);
    }
    @Override
    protected void registerPackets() {
        super.registerPackets();
        tagRewriter.registerGeneric(ClientboundPackets1_21.UPDATE_TAGS);
        tagRewriter.registerGeneric(ClientboundConfigurationPackets1_21.UPDATE_TAGS);
        final SoundRewriter<ClientboundPacket1_21> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_21.SOUND);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_21.SOUND_ENTITY);
        new StatisticsRewriter<>(this).register(ClientboundPackets1_21.AWARD_STATS);
        new AttributeRewriter<>(this).register1_21(ClientboundPackets1_21.UPDATE_ATTRIBUTES);
        /*new CommandRewriter1_19_4<>(this) {
            @Override
            public void handleArgument(final PacketWrapper wrapper, final String argumentType) {
                if (argumentType.equals("minecraft:abc")) {
                    wrapper.write(Types.INT, 0);
                } else {
                    super.handleArgument(wrapper, argumentType);
                }
            }
        }.registerDeclareCommands1_19(ClientboundPackets1_21.COMMANDS);*/
    }
    @Override
    protected void onMappingDataLoaded() {
        /*Types1_21.PARTICLE.filler(this)
                .reader("block", ParticleType.Readers.BLOCK)
                .reader("block_marker", ParticleType.Readers.BLOCK)
                .reader("dust", ParticleType.Readers.DUST)
                .reader("falling_dust", ParticleType.Readers.BLOCK)
                .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
                .reader("item", ParticleType.Readers.ITEM1_20_2)
                .reader("vibration", ParticleType.Readers.VIBRATION1_20_3)
                .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
                .reader("shriek", ParticleType.Readers.SHRIEK);*/
        super.onMappingDataLoaded(); 
    }
    @Override
    public void init(final UserConnection connection) {
        addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_20_5.PLAYER));
    }
    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }
    @Override
    public EntityPacketRewriter1_99 getEntityRewriter() {
        return entityRewriter;
    }
    @Override
    public BlockItemPacketRewriter1_99 getItemRewriter() {
        return itemRewriter;
    }
    @Override
    public TagRewriter<ClientboundPacket1_21> getTagRewriter() {
        return tagRewriter;
    }
    @Override
    protected PacketTypesProvider<ClientboundPacket1_21, ClientboundPacket1_21, ServerboundPacket1_20_5, ServerboundPacket1_20_5> createPacketTypesProvider() {
        return new SimplePacketTypesProvider<>(
            packetTypeMap(unmappedClientboundPacketType, ClientboundPackets1_21.class, ClientboundConfigurationPackets1_21.class),
            packetTypeMap(mappedClientboundPacketType, ClientboundPackets1_21.class, ClientboundConfigurationPackets1_21.class),
            packetTypeMap(mappedServerboundPacketType, ServerboundPackets1_20_5.class, ServerboundConfigurationPackets1_20_5.class),
            packetTypeMap(unmappedServerboundPacketType, ServerboundPackets1_20_5.class, ServerboundConfigurationPackets1_20_5.class)
        );
    }
}

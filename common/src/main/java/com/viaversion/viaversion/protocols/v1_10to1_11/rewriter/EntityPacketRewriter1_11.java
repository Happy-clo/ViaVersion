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
package com.viaversion.viaversion.protocols.v1_10to1_11.rewriter;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_11;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_11.EntityType;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_9;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_9;
import com.viaversion.viaversion.protocols.v1_10to1_11.Protocol1_10To1_11;
import com.viaversion.viaversion.protocols.v1_10to1_11.data.BlockEntityMappings1_11;
import com.viaversion.viaversion.protocols.v1_10to1_11.data.EntityMappings1_11;
import com.viaversion.viaversion.protocols.v1_10to1_11.storage.EntityTracker1_11;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
public class EntityPacketRewriter1_11 extends EntityRewriter<ClientboundPackets1_9_3, Protocol1_10To1_11> {
    public EntityPacketRewriter1_11(Protocol1_10To1_11 protocol) {
        super(protocol);
    }
    @Override
    protected void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_9_3.ADD_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.UUID); 
                map(Types.BYTE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types.INT); 
                handler(wrapper -> {
                    byte type = wrapper.get(Types.BYTE, 0);
                    if (type == EntityTypes1_10.ObjectType.FISHIHNG_HOOK.getId()) {
                        tryFixFishingHookVelocity(wrapper);
                    }
                });
                handler(objectTrackerHandler());
            }
        });
        protocol.registerClientbound(ClientboundPackets1_9_3.ADD_MOB, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.UUID); 
                map(Types.UNSIGNED_BYTE, Types.VAR_INT); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types.SHORT); 
                map(Types.SHORT); 
                map(Types.SHORT); 
                map(Types1_9.ENTITY_DATA_LIST); 
                handler(wrapper -> {
                    int entityId = wrapper.get(Types.VAR_INT, 0);
                    int type = wrapper.get(Types.VAR_INT, 1);
                    EntityTypes1_11.EntityType entType = rewriteEntityType(type, wrapper.get(Types1_9.ENTITY_DATA_LIST, 0));
                    if (entType != null) {
                        wrapper.set(Types.VAR_INT, 1, entType.getId());
                        wrapper.user().getEntityTracker(Protocol1_10To1_11.class).addEntity(entityId, entType);
                        handleEntityData(entityId, wrapper.get(Types1_9.ENTITY_DATA_LIST, 0), wrapper.user());
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_9_3.TAKE_ITEM_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.VAR_INT); 
                handler(wrapper -> {
                    wrapper.write(Types.VAR_INT, 1); 
                });
            }
        });
        registerSetEntityData(ClientboundPackets1_9_3.SET_ENTITY_DATA, Types1_9.ENTITY_DATA_LIST);
        protocol.registerClientbound(ClientboundPackets1_9_3.TELEPORT_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types.BOOLEAN); 
                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    if (Via.getConfig().isHologramPatch()) {
                        EntityTracker1_11 tracker = wrapper.user().getEntityTracker(Protocol1_10To1_11.class);
                        if (tracker.isHologram(entityID)) {
                            Double newValue = wrapper.get(Types.DOUBLE, 1);
                            newValue -= (Via.getConfig().getHologramYOffset());
                            wrapper.set(Types.DOUBLE, 1, newValue);
                        }
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_9_3.SET_ENTITY_MOTION, wrapper -> {
            final int entityId = wrapper.passthrough(Types.VAR_INT);
            if (tracker(wrapper.user()).entityType(entityId) == EntityTypes1_10.EntityType.FISHING_HOOK) {
                tryFixFishingHookVelocity(wrapper);
            }
        });
        registerRemoveEntities(ClientboundPackets1_9_3.REMOVE_ENTITIES);
        protocol.registerClientbound(ClientboundPackets1_9_3.BLOCK_ENTITY_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BLOCK_POSITION1_8); 
                map(Types.UNSIGNED_BYTE); 
                map(Types.NAMED_COMPOUND_TAG); 
                handler(wrapper -> {
                    CompoundTag tag = wrapper.get(Types.NAMED_COMPOUND_TAG, 0);
                    if (wrapper.get(Types.UNSIGNED_BYTE, 0) == 1) {
                        EntityMappings1_11.toClientSpawner(tag);
                    }
                    StringTag idTag = tag.getStringTag("id");
                    if (idTag != null) {
                        idTag.setValue(BlockEntityMappings1_11.toNewIdentifier(idTag.getValue()));
                    }
                });
            }
        });
    }
    @Override
    protected void registerRewrites() {
        filter().handler((event, data) -> {
            if (data.getValue() instanceof DataItem) {
                EntityMappings1_11.toClientItem(data.value());
            }
        });
        filter().type(EntityType.GUARDIAN).index(12).handler((event, data) -> {
            boolean value = (((byte) data.getValue()) & 0x02) == 0x02;
            data.setTypeAndValue(EntityDataTypes1_9.BOOLEAN, value);
        });
        filter().type(EntityType.ABSTRACT_SKELETON).removeIndex(12);
        filter().type(EntityType.ZOMBIE).handler((event, data) -> {
            if ((event.entityType() == EntityType.ZOMBIE || event.entityType() == EntityType.HUSK) && data.id() == 14) {
                event.cancel();
            } else if (data.id() == 15) {
                data.setId(14);
            }
        });
        filter().type(EntityType.ABSTRACT_HORSE).handler((event, data) -> {
            final com.viaversion.viaversion.api.minecraft.entities.EntityType type = event.entityType();
            int id = data.id();
            if (id == 14) { 
                event.cancel();
                return;
            }
            if (id == 16) { 
                data.setId(14);
            } else if (id == 17) { 
                data.setId(16);
            }
            if (!type.is(EntityType.HORSE) && data.id() == 15 || data.id() == 16) {
                event.cancel();
                return;
            }
            if ((type == EntityType.DONKEY || type == EntityType.MULE) && data.id() == 13) {
                if ((((byte) data.getValue()) & 0x08) == 0x08) {
                    event.createExtraData(new EntityData(15, EntityDataTypes1_9.BOOLEAN, true));
                } else {
                    event.createExtraData(new EntityData(15, EntityDataTypes1_9.BOOLEAN, false));
                }
            }
        });
        filter().type(EntityType.ARMOR_STAND).index(0).handler((event, data) -> {
            if (!Via.getConfig().isHologramPatch()) {
                return;
            }
            EntityData flags = event.dataAtIndex(11);
            EntityData customName = event.dataAtIndex(2);
            EntityData customNameVisible = event.dataAtIndex(3);
            if (flags == null || customName == null || customNameVisible == null) {
                return;
            }
            byte value = data.value();
            if ((value & 0x20) == 0x20 && ((byte) flags.getValue() & 0x01) == 0x01
                && !((String) customName.getValue()).isEmpty() && (boolean) customNameVisible.getValue()) {
                EntityTracker1_11 tracker = tracker(event.user());
                int entityId = event.entityId();
                if (tracker.addHologram(entityId)) {
                    PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9_3.MOVE_ENTITY_POS, null, event.user());
                    wrapper.write(Types.VAR_INT, entityId);
                    wrapper.write(Types.SHORT, (short) 0);
                    wrapper.write(Types.SHORT, (short) (128D * (-Via.getConfig().getHologramYOffset() * 32D)));
                    wrapper.write(Types.SHORT, (short) 0);
                    wrapper.write(Types.BOOLEAN, true);
                    wrapper.send(Protocol1_10To1_11.class);
                }
            }
        });
    }
    private void tryFixFishingHookVelocity(final PacketWrapper wrapper) {
        final short x = wrapper.read(Types.SHORT);
        final short y = wrapper.read(Types.SHORT);
        final short z = wrapper.read(Types.SHORT);
        wrapper.write(Types.SHORT, (short) (x * 1.33));
        wrapper.write(Types.SHORT, (short) (y * 1.2));
        wrapper.write(Types.SHORT, (short) (z * 1.33));
    }
    @Override
    public com.viaversion.viaversion.api.minecraft.entities.EntityType typeFromId(int type) {
        return EntityTypes1_11.getTypeFromId(type, false);
    }
    @Override
    public com.viaversion.viaversion.api.minecraft.entities.EntityType objectTypeFromId(int type) {
        return EntityTypes1_11.getTypeFromId(type, true);
    }
    public EntityType rewriteEntityType(int numType, List<EntityData> entityData) {
        EntityType type = EntityType.findById(numType);
        if (type == null) {
            Via.getManager().getPlatform().getLogger().severe("Error: could not find Entity type " + numType + " with entity data: " + entityData);
            return null;
        }
        try {
            if (type.is(EntityType.GUARDIAN)) {
                Optional<EntityData> options = getById(entityData, 12);
                if (options.isPresent()) {
                    if ((((byte) options.get().getValue()) & 0x04) == 0x04) {
                        return EntityType.ELDER_GUARDIAN;
                    }
                }
            }
            if (type.is(EntityType.SKELETON)) {
                Optional<EntityData> options = getById(entityData, 12);
                if (options.isPresent()) {
                    if (((int) options.get().getValue()) == 1) {
                        return EntityType.WITHER_SKELETON;
                    }
                    if (((int) options.get().getValue()) == 2) {
                        return EntityType.STRAY;
                    }
                }
            }
            if (type.is(EntityType.ZOMBIE)) {
                Optional<EntityData> options = getById(entityData, 13);
                if (options.isPresent()) {
                    int value = (int) options.get().getValue();
                    if (value > 0 && value < 6) {
                        entityData.add(new EntityData(16, EntityDataTypes1_9.VAR_INT, value - 1)); 
                        return EntityType.ZOMBIE_VILLAGER;
                    }
                    if (value == 6) {
                        return EntityType.HUSK;
                    }
                }
            }
            if (type.is(EntityType.HORSE)) {
                Optional<EntityData> options = getById(entityData, 14);
                if (options.isPresent()) {
                    if (((int) options.get().getValue()) == 0) {
                        return EntityType.HORSE;
                    }
                    if (((int) options.get().getValue()) == 1) {
                        return EntityType.DONKEY;
                    }
                    if (((int) options.get().getValue()) == 2) {
                        return EntityType.MULE;
                    }
                    if (((int) options.get().getValue()) == 3) {
                        return EntityType.ZOMBIE_HORSE;
                    }
                    if (((int) options.get().getValue()) == 4) {
                        return EntityType.SKELETON_HORSE;
                    }
                }
            }
        } catch (Exception e) {
            if (!Via.getConfig().isSuppressMetadataErrors() || Via.getManager().isDebug()) {
                protocol.getLogger().warning("An error occurred with entity type rewriter");
                protocol.getLogger().warning("Entity data: " + entityData);
                protocol.getLogger().log(Level.WARNING, "Error: ", e);
            }
        }
        return type;
    }
    public Optional<EntityData> getById(List<EntityData> entityData, int id) {
        for (EntityData data : entityData) {
            if (data.id() == id) return Optional.of(data);
        }
        return Optional.empty();
    }
}

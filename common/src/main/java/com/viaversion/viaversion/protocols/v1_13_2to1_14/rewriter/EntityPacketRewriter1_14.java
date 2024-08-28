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
package com.viaversion.viaversion.protocols.v1_13_2to1_14.rewriter;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.VillagerData;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_14;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_13_2;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.Protocol1_13_2To1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.storage.EntityTracker1_14;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import java.util.ArrayList;
import java.util.List;
public class EntityPacketRewriter1_14 extends EntityRewriter<ClientboundPackets1_13, Protocol1_13_2To1_14> {
    public EntityPacketRewriter1_14(Protocol1_13_2To1_14 protocol) {
        super(protocol);
    }
    @Override
    protected void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_13.ADD_EXPERIENCE_ORB, wrapper -> {
            int entityId = wrapper.passthrough(Types.VAR_INT);
            tracker(wrapper.user()).addEntity(entityId, EntityTypes1_14.EXPERIENCE_ORB);
        });
        protocol.registerClientbound(ClientboundPackets1_13.ADD_GLOBAL_ENTITY, wrapper -> {
            int entityId = wrapper.passthrough(Types.VAR_INT);
            if (wrapper.passthrough(Types.BYTE) == 1) {
                tracker(wrapper.user()).addEntity(entityId, EntityTypes1_14.LIGHTNING_BOLT);
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.ADD_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.UUID); 
                map(Types.BYTE, Types.VAR_INT); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types.INT); 
                map(Types.SHORT); 
                map(Types.SHORT); 
                map(Types.SHORT); 
                handler(wrapper -> {
                    int entityId = wrapper.get(Types.VAR_INT, 0);
                    int typeId = wrapper.get(Types.VAR_INT, 1);
                    EntityTypes1_13.EntityType type1_13 = EntityTypes1_13.getTypeFromId(typeId, true);
                    typeId = newEntityId(type1_13.getId());
                    EntityType type1_14 = EntityTypes1_14.getTypeFromId(typeId);
                    if (type1_14 != null) {
                        int data = wrapper.get(Types.INT, 0);
                        if (type1_14.is(EntityTypes1_14.FALLING_BLOCK)) {
                            wrapper.set(Types.INT, 0, protocol.getMappingData().getNewBlockStateId(data));
                        } else if (type1_14.is(EntityTypes1_14.MINECART)) {
                            typeId = switch (data) {
                                case 1 -> EntityTypes1_14.CHEST_MINECART.getId();
                                case 2 -> EntityTypes1_14.FURNACE_MINECART.getId();
                                case 3 -> EntityTypes1_14.TNT_MINECART.getId();
                                case 4 -> EntityTypes1_14.SPAWNER_MINECART.getId();
                                case 5 -> EntityTypes1_14.HOPPER_MINECART.getId();
                                case 6 -> EntityTypes1_14.COMMAND_BLOCK_MINECART.getId();
                                default -> typeId; 
                            };
                        } else if ((type1_14.is(EntityTypes1_14.ITEM) && data > 0)
                            || type1_14.isOrHasParent(EntityTypes1_14.ABSTRACT_ARROW)) {
                            if (type1_14.isOrHasParent(EntityTypes1_14.ABSTRACT_ARROW)) {
                                wrapper.set(Types.INT, 0, data - 1);
                            }
                            PacketWrapper velocity = wrapper.create(ClientboundPackets1_14.SET_ENTITY_MOTION);
                            velocity.write(Types.VAR_INT, entityId);
                            velocity.write(Types.SHORT, wrapper.get(Types.SHORT, 0));
                            velocity.write(Types.SHORT, wrapper.get(Types.SHORT, 1));
                            velocity.write(Types.SHORT, wrapper.get(Types.SHORT, 2));
                            velocity.scheduleSend(Protocol1_13_2To1_14.class);
                        }
                        wrapper.user().getEntityTracker(Protocol1_13_2To1_14.class).addEntity(entityId, type1_14);
                    }
                    wrapper.set(Types.VAR_INT, 1, typeId);
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.ADD_MOB, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.UUID); 
                map(Types.VAR_INT); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types.SHORT); 
                map(Types.SHORT); 
                map(Types.SHORT); 
                map(Types1_13_2.ENTITY_DATA_LIST, Types1_14.ENTITY_DATA_LIST); 
                handler(trackerAndRewriterHandler(Types1_14.ENTITY_DATA_LIST));
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.ADD_PAINTING, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT);
                map(Types.UUID);
                map(Types.VAR_INT);
                map(Types.BLOCK_POSITION1_8, Types.BLOCK_POSITION1_14);
                map(Types.BYTE);
                handler(wrapper -> tracker(wrapper.user()).addEntity(wrapper.get(Types.VAR_INT, 0), EntityTypes1_14.PAINTING));
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.ADD_PLAYER, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); 
                map(Types.UUID); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.DOUBLE); 
                map(Types.BYTE); 
                map(Types.BYTE); 
                map(Types1_13_2.ENTITY_DATA_LIST, Types1_14.ENTITY_DATA_LIST); 
                handler(trackerAndRewriterHandler(Types1_14.ENTITY_DATA_LIST, EntityTypes1_14.PLAYER));
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.ANIMATE, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT);
                handler(wrapper -> {
                    short animation = wrapper.passthrough(Types.UNSIGNED_BYTE);
                    if (animation == 2) {  
                        EntityTracker1_14 tracker = wrapper.user().getEntityTracker(Protocol1_13_2To1_14.class);
                        int entityId = wrapper.get(Types.VAR_INT, 0);
                        tracker.setSleeping(entityId, false);
                        PacketWrapper entityDataPacket = wrapper.create(ClientboundPackets1_14.SET_ENTITY_DATA);
                        entityDataPacket.write(Types.VAR_INT, entityId);
                        List<EntityData> entityDataList = new ArrayList<>();
                        if (tracker.clientEntityId() != entityId) {
                            entityDataList.add(new EntityData(6, Types1_14.ENTITY_DATA_TYPES.poseType, EntityPacketRewriter1_14.recalculatePlayerPose(entityId, tracker)));
                        }
                        entityDataList.add(new EntityData(12, Types1_14.ENTITY_DATA_TYPES.optionalBlockPositionType, null));
                        entityDataPacket.write(Types1_14.ENTITY_DATA_LIST, entityDataList);
                        entityDataPacket.scheduleSend(Protocol1_13_2To1_14.class);
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); 
                map(Types.UNSIGNED_BYTE); 
                map(Types.INT); 
                handler(wrapper -> {
                    ClientWorld clientChunks = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Types.INT, 1);
                    clientChunks.setEnvironment(dimensionId);
                });
                handler(playerTrackerHandler());
                handler(wrapper -> {
                    short difficulty = wrapper.read(Types.UNSIGNED_BYTE); 
                    PacketWrapper difficultyPacket = wrapper.create(ClientboundPackets1_14.CHANGE_DIFFICULTY);
                    difficultyPacket.write(Types.UNSIGNED_BYTE, difficulty);
                    difficultyPacket.write(Types.BOOLEAN, false); 
                    difficultyPacket.scheduleSend(protocol.getClass());
                    wrapper.passthrough(Types.UNSIGNED_BYTE); 
                    wrapper.passthrough(Types.STRING); 
                    wrapper.write(Types.VAR_INT, WorldPacketRewriter1_14.SERVERSIDE_VIEW_DISTANCE);  
                });
                handler(wrapper -> {
                    wrapper.send(Protocol1_13_2To1_14.class);
                    wrapper.cancel();
                    WorldPacketRewriter1_14.sendViewDistancePacket(wrapper.user());
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.PLAYER_SLEEP, ClientboundPackets1_14.SET_ENTITY_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT);
                handler(wrapper -> {
                    EntityTracker1_14 tracker = wrapper.user().getEntityTracker(Protocol1_13_2To1_14.class);
                    int entityId = wrapper.get(Types.VAR_INT, 0);
                    tracker.setSleeping(entityId, true);
                    BlockPosition position = wrapper.read(Types.BLOCK_POSITION1_8);
                    List<EntityData> entityDataList = new ArrayList<>();
                    entityDataList.add(new EntityData(12, Types1_14.ENTITY_DATA_TYPES.optionalBlockPositionType, position));
                    if (tracker.clientEntityId() != entityId) {
                        entityDataList.add(new EntityData(6, Types1_14.ENTITY_DATA_TYPES.poseType, EntityPacketRewriter1_14.recalculatePlayerPose(entityId, tracker)));
                    }
                    wrapper.write(Types1_14.ENTITY_DATA_LIST, entityDataList);
                });
            }
        });
        registerRemoveEntities(ClientboundPackets1_13.REMOVE_ENTITIES);
        registerSetEntityData(ClientboundPackets1_13.SET_ENTITY_DATA, Types1_13_2.ENTITY_DATA_LIST, Types1_14.ENTITY_DATA_LIST);
    }
    @Override
    protected void registerRewrites() {
        filter().mapDataType(Types1_14.ENTITY_DATA_TYPES::byId);
        registerEntityDataTypeHandler(Types1_14.ENTITY_DATA_TYPES.itemType, Types1_14.ENTITY_DATA_TYPES.optionalBlockStateType, Types1_14.ENTITY_DATA_TYPES.particleType);
        filter().type(EntityTypes1_14.ENTITY).addIndex(6);
        registerBlockStateHandler(EntityTypes1_14.ABSTRACT_MINECART, 10);
        filter().type(EntityTypes1_14.LIVING_ENTITY).addIndex(12);
        filter().type(EntityTypes1_14.LIVING_ENTITY).index(8).handler((event, data) -> {
            float value = ((Number) data.getValue()).floatValue();
            if (Float.isNaN(value) && Via.getConfig().is1_14HealthNaNFix()) {
                data.setValue(1F);
            }
        });
        filter().type(EntityTypes1_14.MOB).index(13).handler((event, data) -> {
            EntityTracker1_14 tracker = tracker(event.user());
            int entityId = event.entityId();
            tracker.setInsentientData(entityId, (byte) ((((Number) data.getValue()).byteValue() & ~0x4)
                | (tracker.getInsentientData(entityId) & 0x4))); 
            data.setValue(tracker.getInsentientData(entityId));
        });
        filter().type(EntityTypes1_14.PLAYER).handler((event, data) -> {
            EntityTracker1_14 tracker = tracker(event.user());
            int entityId = event.entityId();
            if (entityId != tracker.clientEntityId()) {
                if (data.id() == 0) {
                    byte flags = ((Number) data.getValue()).byteValue();
                    tracker.setEntityFlags(entityId, flags);
                } else if (data.id() == 7) {
                    tracker.setRiptide(entityId, (((Number) data.getValue()).byteValue() & 0x4) != 0);
                }
                if (data.id() == 0 || data.id() == 7) {
                    event.createExtraData(new EntityData(6, Types1_14.ENTITY_DATA_TYPES.poseType, recalculatePlayerPose(entityId, tracker)));
                }
            }
        });
        filter().type(EntityTypes1_14.ZOMBIE).handler((event, data) -> {
            if (data.id() == 16) {
                EntityTracker1_14 tracker = tracker(event.user());
                int entityId = event.entityId();
                tracker.setInsentientData(entityId, (byte) ((tracker.getInsentientData(entityId) & ~0x4)
                    | ((boolean) data.getValue() ? 0x4 : 0))); 
                event.createExtraData(new EntityData(13, Types1_14.ENTITY_DATA_TYPES.byteType, tracker.getInsentientData(entityId)));
                event.cancel(); 
            } else if (data.id() > 16) {
                data.setId(data.id() - 1);
            }
        });
        filter().type(EntityTypes1_14.HORSE).index(18).handler((event, data) -> {
            event.cancel();
            int armorType = data.value();
            Item armorItem = null;
            if (armorType == 1) {  
                armorItem = new DataItem(protocol.getMappingData().getNewItemId(727), (byte) 1, null);
            } else if (armorType == 2) {  
                armorItem = new DataItem(protocol.getMappingData().getNewItemId(728), (byte) 1, null);
            } else if (armorType == 3) {  
                armorItem = new DataItem(protocol.getMappingData().getNewItemId(729), (byte) 1, null);
            }
            PacketWrapper equipmentPacket = PacketWrapper.create(ClientboundPackets1_14.SET_EQUIPPED_ITEM, null, event.user());
            equipmentPacket.write(Types.VAR_INT, event.entityId());
            equipmentPacket.write(Types.VAR_INT, 4);
            equipmentPacket.write(Types.ITEM1_13_2, armorItem);
            try {
                equipmentPacket.scheduleSend(Protocol1_13_2To1_14.class);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
        filter().type(EntityTypes1_14.VILLAGER).index(15).handler((event, data) -> {
            data.setTypeAndValue(Types1_14.ENTITY_DATA_TYPES.villagerDatatType, new VillagerData(2, getNewProfessionId(data.value()), 0));
        });
        filter().type(EntityTypes1_14.ZOMBIE_VILLAGER).index(18).handler((event, data) -> {
            data.setTypeAndValue(Types1_14.ENTITY_DATA_TYPES.villagerDatatType, new VillagerData(2, getNewProfessionId(data.value()), 0));
        });
        filter().type(EntityTypes1_14.ABSTRACT_ARROW).addIndex(9); 
        filter().type(EntityTypes1_14.FIREWORK_ROCKET).index(8).handler((event, data) -> {
            data.setDataType(Types1_14.ENTITY_DATA_TYPES.optionalVarIntType);
            if (data.getValue().equals(0)) {
                data.setValue(null); 
            }
        });
        filter().type(EntityTypes1_14.ABSTRACT_SKELETON).index(14).handler((event, data) -> {
            EntityTracker1_14 tracker = tracker(event.user());
            int entityId = event.entityId();
            tracker.setInsentientData(entityId, (byte) ((tracker.getInsentientData(entityId) & ~0x4)
                | ((boolean) data.getValue() ? 0x4 : 0))); 
            event.createExtraData(new EntityData(13, Types1_14.ENTITY_DATA_TYPES.byteType, tracker.getInsentientData(entityId)));
            event.cancel();  
        });
        filter().type(EntityTypes1_14.ABSTRACT_ILLAGER).handler((event, data) -> {
            if (event.index() == 14) {
                EntityTracker1_14 tracker = tracker(event.user());
                int entityId = event.entityId();
                tracker.setInsentientData(entityId, (byte) ((tracker.getInsentientData(entityId) & ~0x4)
                    | (((Number) data.getValue()).byteValue() != 0 ? 0x4 : 0))); 
                event.createExtraData(new EntityData(13, Types1_14.ENTITY_DATA_TYPES.byteType, tracker.getInsentientData(entityId)));
                event.cancel(); 
            } else if (event.index() > 14) {
                data.setId(data.id() - 1);
            }
        });
        filter().type(EntityTypes1_14.OCELOT).removeIndex(17); 
        filter().type(EntityTypes1_14.OCELOT).removeIndex(16); 
        filter().type(EntityTypes1_14.OCELOT).removeIndex(15); 
        filter().type(EntityTypes1_14.ABSTRACT_RAIDER).addIndex(14); 
    }
    @Override
    public void onMappingDataLoaded() {
        mapTypes();
        if (Via.getConfig().translateOcelotToCat()) {
            mapEntityType(EntityTypes1_13.EntityType.OCELOT, EntityTypes1_14.CAT);
        }
    }
    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_14.getTypeFromId(type);
    }
    private static boolean isSneaking(byte flags) {
        return (flags & 0x2) != 0;
    }
    private static boolean isSwimming(byte flags) {
        return (flags & 0x10) != 0;
    }
    private static int getNewProfessionId(int old) {
        return switch (old) {
            case 0 -> 5; 
            case 1 -> 9; 
            case 2 -> 4; 
            case 3 -> 1; 
            case 4 -> 2; 
            case 5 -> 11; 
            default -> 0; 
        };
    }
    private static boolean isFallFlying(int entityFlags) {
        return (entityFlags & 0x80) != 0;
    }
    public static int recalculatePlayerPose(int entityId, EntityTracker1_14 tracker) {
        byte flags = tracker.getEntityFlags(entityId);
        int pose = 0; 
        if (isFallFlying(flags)) {
            pose = 1;
        } else if (tracker.isSleeping(entityId)) {
            pose = 2;
        } else if (isSwimming(flags)) {
            pose = 3;
        } else if (tracker.isRiptide(entityId)) {
            pose = 4;
        } else if (isSneaking(flags)) {
            pose = 5;
        }
        return pose;
    }
}

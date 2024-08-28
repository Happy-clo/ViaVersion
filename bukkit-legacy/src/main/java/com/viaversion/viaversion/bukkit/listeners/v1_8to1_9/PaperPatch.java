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
package com.viaversion.viaversion.bukkit.listeners.v1_8to1_9;
import com.viaversion.viaversion.bukkit.listeners.ViaBukkitListener;
import com.viaversion.viaversion.bukkit.util.CollisionChecker;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;
public class PaperPatch extends ViaBukkitListener {
    private final CollisionChecker CHECKER = CollisionChecker.getInstance();
    public PaperPatch(Plugin plugin) {
        super(plugin, Protocol1_8To1_9.class);
    }
    /*
    This patch is applied when Paper is detected.
    I'm unsure of what causes this but essentially,
    placing blocks where you're standing works?
    If there is a better fix then we'll replace this.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent e) {
        if (!isOnPipe(e.getPlayer())) return;
        if (CHECKER != null) {
            Boolean intersect = CHECKER.intersects(e.getBlockPlaced(), e.getPlayer());
            if (intersect != null) {
                if (intersect) e.setCancelled(true);
                return;
            }
        }
        Material block = e.getBlockPlaced().getType();
        if (isPlacable(block)) {
            return;
        }
        Location location = e.getPlayer().getLocation();
        Block locationBlock = location.getBlock();
        if (locationBlock.equals(e.getBlock())) {
            e.setCancelled(true);
        } else {
            if (locationBlock.getRelative(BlockFace.UP).equals(e.getBlock())) {
                e.setCancelled(true);
            } else {
                Location diff = location.clone().subtract(e.getBlock().getLocation().add(0.5D, 0, 0.5D));
                if (Math.abs(diff.getX()) <= 0.8 && Math.abs(diff.getZ()) <= 0.8D) {
                    if (diff.getY() <= 0.1D && diff.getY() >= -0.1D) {
                        e.setCancelled(true);
                        return;
                    }
                    BlockFace relative = e.getBlockAgainst().getFace(e.getBlock());
                    if (relative == BlockFace.UP) {
                        if (diff.getY() < 1D && diff.getY() >= 0D) {
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
    private boolean isPlacable(Material material) {
        if (!material.isSolid()) return true;
        switch (material.getId()) {
            case 63: 
            case 68: 
            case 70: 
            case 72: 
            case 147: 
            case 148: 
            case 176: 
            case 177: 
                return true;
            default:
                return false;
        }
    }
}

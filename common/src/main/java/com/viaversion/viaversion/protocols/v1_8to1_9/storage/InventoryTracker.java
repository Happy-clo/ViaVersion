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
package com.viaversion.viaversion.protocols.v1_8to1_9.storage;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import java.util.HashMap;
import java.util.Map;
public class InventoryTracker implements StorableObject {
    private String inventory;
    private final Map<Short, Map<Short, Integer>> windowItemCache = new HashMap<>();
    private int itemIdInCursor;
    private boolean dragging;
    public String getInventory() {
        return inventory;
    }
    public void setInventory(String inventory) {
        this.inventory = inventory;
    }
    public void resetInventory(short windowId) {
        if (inventory == null) {
            this.itemIdInCursor = 0;
            this.dragging = false;
            if (windowId != 0) {
                this.windowItemCache.remove(windowId);
            }
        }
    }
    public int getItemId(short windowId, short slot) {
        Map<Short, Integer> itemMap = this.windowItemCache.get(windowId);
        if (itemMap == null) {
            return 0;
        }
        return itemMap.getOrDefault(slot, 0);
    }
    public void setItemId(short windowId, short slot, int itemId) {
        if (windowId == -1 && slot == -1) {
            this.itemIdInCursor = itemId;
        } else {
            this.windowItemCache.computeIfAbsent(windowId, k -> new HashMap<>()).put(slot, itemId);
        }
    }
    /**
     * Handle the window click to track the position of the sword
     *
     * @param windowId  Id of the current inventory
     * @param mode      Inventory operation mode
     * @param hoverSlot The slot number of the current mouse position
     * @param button    The button to use in the click
     */
    public void handleWindowClick(UserConnection user, short windowId, byte mode, short hoverSlot, byte button) {
        EntityTracker1_9 entityTracker = user.getEntityTracker(Protocol1_8To1_9.class);
        if (hoverSlot == -1) {
            return;
        }
        if (hoverSlot == 45) {
            entityTracker.setSecondHand(null); 
            return;
        }
        boolean isArmorOrResultSlot = hoverSlot >= 5 && hoverSlot <= 8 || hoverSlot == 0;
        switch (mode) {
            case 0: 
                if (this.itemIdInCursor == 0) {
                    this.itemIdInCursor = getItemId(windowId, hoverSlot);
                    setItemId(windowId, hoverSlot, 0);
                } else {
                    if (hoverSlot == -999) {
                        this.itemIdInCursor = 0;
                    } else if (!isArmorOrResultSlot) {
                        int previousItem = getItemId(windowId, hoverSlot);
                        setItemId(windowId, hoverSlot, this.itemIdInCursor);
                        this.itemIdInCursor = previousItem;
                    }
                }
                break;
            case 2: 
                if (!isArmorOrResultSlot) {
                    short hotkeySlot = (short) (button + 36);
                    int sourceItem = getItemId(windowId, hoverSlot);
                    int destinationItem = getItemId(windowId, hotkeySlot);
                    setItemId(windowId, hotkeySlot, sourceItem);
                    setItemId(windowId, hoverSlot, destinationItem);
                }
                break;
            case 4: 
                int hoverItem = getItemId(windowId, hoverSlot);
                if (hoverItem != 0) {
                    setItemId(windowId, hoverSlot, 0);
                }
                break;
            case 5: 
                switch (button) {
                    case 0, 4: 
                        this.dragging = true;
                        break;
                    case 1, 5: 
                        if (this.dragging && this.itemIdInCursor != 0 && !isArmorOrResultSlot) {
                            int previousItem = getItemId(windowId, hoverSlot);
                            setItemId(windowId, hoverSlot, this.itemIdInCursor);
                            this.itemIdInCursor = previousItem;
                        }
                        break;
                    case 2, 6: 
                        this.dragging = false;
                        break;
                }
                break;
            default:
                break;
        }
        entityTracker.syncShieldWithSword();
    }
}

package com.controlj.addon.zonehistory.cache;

import com.controlj.green.addonsupport.access.Location;

/**
 *
 */
public class ZoneHistory {
    private final Location equipmentColorLocation;

    public ZoneHistory(Location equipmentColorLocation) {
        this.equipmentColorLocation = equipmentColorLocation;
    }

    public Location getEquipmentColorLocation() {
        return equipmentColorLocation;
    }

}

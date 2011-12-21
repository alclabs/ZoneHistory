package com.controlj.addon.zonehistory.cache;

import com.controlj.green.addonsupport.access.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public enum ZoneHistoryCache {
    INSTANCE;

    private Map<String, Collection<ZoneHistory>> ancestryMap = new HashMap<String, Collection<ZoneHistory>>();
    private Map<String, ZoneHistory> zoneHistoryMap = new HashMap<String, ZoneHistory>();

    public Collection<ZoneHistory> getDescendantZoneHistories(Location loc) {
        String lus = loc.getTransientLookupString();

        synchronized (this) {
            return ancestryMap.get(lus);
        }
    }

    public Collection<ZoneHistory> addDescendantZoneHistories(Location loc, Collection<ZoneHistory> newHistories) {
        Collection<ZoneHistory> result;
        String lus = loc.getTransientLookupString();
        synchronized (this) {
            Collection<ZoneHistory> existingHistories = ancestryMap.get(lus);
            if (existingHistories == null) {
                result = new ArrayList<ZoneHistory>();
                for (ZoneHistory newHistory : newHistories) {
                    result.add(getOfficialZoneHistory(newHistory));
                }
                ancestryMap.put(lus, result);
            } else {
                result = existingHistories;
            }
            return result;
        }
    }

    private ZoneHistory getOfficialZoneHistory(ZoneHistory newHistory) {
        String lus = newHistory.getEquipmentColorLookupString();
        synchronized (this) {
            ZoneHistory zoneHistory = zoneHistoryMap.get(lus);
            if (zoneHistory == null) {
                zoneHistoryMap.put(lus, newHistory);
                zoneHistory = newHistory;
            }
            return zoneHistory;
        }
    }

    public synchronized void reset() {
        ancestryMap.clear();
        zoneHistoryMap.clear();
    }
}

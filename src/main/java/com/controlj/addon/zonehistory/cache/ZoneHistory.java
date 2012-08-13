package com.controlj.addon.zonehistory.cache;

import com.controlj.green.addonsupport.access.EquipmentColor;
import com.controlj.green.addonsupport.access.Location;

import java.util.*;

/**
 *
 */
public class ZoneHistory
{
    private final static long ONE_DAY_MILLIS = 1000L * 60 * 60 * 24;
    private final String equipmentColorLookupString;
    private HashMap<DateRange, Map<EquipmentColor, Long>> cache = new HashMap<DateRange, Map<EquipmentColor, Long>>();

    public ZoneHistory(Location equipmentColorLocation)
    {
        this.equipmentColorLookupString = equipmentColorLocation.getTransientLookupString();
    }

    public String getEquipmentColorLookupString()
    {
        return equipmentColorLookupString;
    }

    public synchronized Map<EquipmentColor, Long> getMapForDates(DateRange range)
    {
        if (range.getStart().equals(getMidnightToday()))
            return null;    // don't cache values for today, they are still changing.

        return cache.get(range);
    }

    public synchronized Map<EquipmentColor, Long> addMap(DateRange range, Map<EquipmentColor, Long> map)
    {
        Map<EquipmentColor, Long> result = cache.get(range);
        if (result == null)
        {
            expireCachedData();
            cache.put(range, map);
            result = map;
        }

        return result;
    }

    public int getCacheSize()
    {
        return cache.size();
    }

    /**
     * Expire old data from the cache based on the current date.  This uses knowledge of
     * the available choices for date ranges.
     */
    private synchronized void expireCachedData()
    {
        Date midnight = getMidnightToday();
        List<DateRange> expiredKeys = null;

        for (DateRange range : cache.keySet())
        {
            if (isExpired(range, midnight))
            {
                if (expiredKeys == null)
                    expiredKeys = new ArrayList<DateRange>();

                expiredKeys.add(range);
            }
        }
        if (expiredKeys != null)
        {
            for (DateRange expiredKey : expiredKeys)
                cache.remove(expiredKey);
        }
    }

    /**
     * Check if a cache entry for the specified range should be expired.  This assumes that none of
     * the ranges in the cache matched this one.
     *
     * @param range         range to check
     * @param midnightToday date of midnight today (that started the day)
     * @return true if it should be expired
     */
    private boolean isExpired(DateRange range, Date midnightToday)
    {
        // if it doesn't end today at midnight, then expire
        // this gets all partial day ranges too
        return (!range.getEnd().equals(midnightToday));
    }

    private static Date getMidnightToday()
    {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }
}

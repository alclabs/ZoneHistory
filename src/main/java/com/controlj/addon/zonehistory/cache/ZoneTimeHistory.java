package com.controlj.addon.zonehistory.cache;

import com.controlj.addon.zonehistory.reports.ReportResultsData;
import com.controlj.green.addonsupport.access.Location;

import java.util.*;

/**
 *
 */
public class ZoneTimeHistory
{
    private final String equipmentColorLookupString;
//    private HashMap<DateRange, Map<EquipmentColor, Long>> cache = new HashMap<DateRange, Map<EquipmentColor, Long>>();
//    private HashMap<DateRange, ReportResultsData> cache = new HashMap<DateRange, ReportResultsData>();
    private final ReportResultsData cache;
    private final Collection<DateRange> unoccupiedTimes;

    public ZoneTimeHistory(Location equipmentColorLocation, ReportResultsData resultsToCache, Collection<DateRange> unoccupiedTimes)
    {
        this.equipmentColorLookupString = equipmentColorLocation.getTransientLookupString();
        this.cache = resultsToCache;
        this.unoccupiedTimes = unoccupiedTimes;
    }

    public String getEquipmentColorLookupString()
    {
        return equipmentColorLookupString;
    }

//    public synchronized ReportResultsData getResultsForDates(DateRange range)
//    {
//        if (range.getStart().equals(getMidnightToday()))
//            return null;    // don't cache values for today, they are still changing.
//
//        return cache.get(range);
//    }


    public ReportResultsData getCachedData()
    {
        return cache;
    }

//    public synchronized ReportResultsData addResults(DateRange range, ReportResultsData results)
//    {
//        ReportResultsData result = cache.get(range);
//        if (result == null)
//        {
//            expireCachedData();
//            cache.put(range, results);
//            result = results;
//        }
//
//        return result;
//    }

//    public synchronized void addUnoccupiedTimes(List<DateRange> ranges)
//    {
//        this.unoccupiedTimes = ranges;
//    }

    public synchronized Collection<DateRange> getUnoccupiedTimes()
    {
        return this.unoccupiedTimes;
    }

//    public int getCacheSize()
//    {
//        return cache.size();
//    }

    /**
     * Expire old data from the cache based on the current date.  This uses knowledge of
     * the available choices for date ranges.
     */
    private synchronized void expireCachedData()
    {
//        Date midnight = getMidnightToday();
//        List<DateRange> expiredKeys = null;

//        for (DateRange range : cache.keySet())
//        {
//            if (isExpired(range, midnight))
//            {
//                if (expiredKeys == null)
//                    expiredKeys = new ArrayList<DateRange>();
//
//                expiredKeys.add(range);
//            }
//        }
//        if (expiredKeys != null)
//        {
//            for (DateRange expiredKey : expiredKeys)
//                cache.remove(expiredKey);
//        }
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

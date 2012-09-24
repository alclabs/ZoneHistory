package com.controlj.addon.zonehistory.cache;

import com.controlj.addon.zonehistory.reports.ReportResultsData;
import com.controlj.green.addonsupport.access.Location;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 *
 */
public enum ZoneHistoryCache
{
    SATISFACTION,
    EI;

    private final static long ONE_DAY_MILLIS = 1000L * 60 * 60 * 24;
    private Map<String, Map<DateRange, ZoneTimeHistory>> zoneHistoryCache = new HashMap<String, Map<DateRange, ZoneTimeHistory>>();
//    private Map<String, ZoneTimeHistory> zoneHistoryMap = new HashMap<String, ZoneTimeHistory>();

    @Nullable
    public ReportResultsData getCachedData(Location loc, DateRange dateRange)
    {
        String lus = loc.getTransientLookupString();

        synchronized (this)
        {
            Map<DateRange, ZoneTimeHistory> zoneHistories = zoneHistoryCache.get(lus);
            if (zoneHistories == null)
            {
                zoneHistoryCache.put(lus, new HashMap<DateRange, ZoneTimeHistory>());
                return null;
            }

            if (zoneHistories.get(dateRange) == null)
                return null;

            return zoneHistories.get(dateRange).getCachedData();


//            if (zoneHistories == null)
//                zoneHistories = Collections.emptyList();
//
//            // here because one source may have multiple zone histories
//            for (ZoneTimeHistory zoneHistory : zoneHistories)
//            {
//                if (zoneHistory.getResultsForDates(dateRange) != null)
//                    return zoneHistory.getResultsForDates(dateRange);
//            }
//
//            return null; // todo - what if no cached data exists?
        }
    }

    public void addZoneTimeHistory(Location loc, DateRange dateRange, ZoneTimeHistory newTimeHistory)
    {
        String lus = loc.getTransientLookupString();
        synchronized (this)
        {
            Map<DateRange, ZoneTimeHistory> currentZoneHistory = this.zoneHistoryCache.get(lus);

            // make sure the days for the DateRange are set to the midnights of their respective start and end dates
            DateRange midnightedDateRange = this.setDateRangeToMidnight(dateRange);
            currentZoneHistory.put(midnightedDateRange, newTimeHistory);
            zoneHistoryCache.put(lus, currentZoneHistory);
//            Collection<ZoneTimeHistory> existingTimeHistories = zoneHistoryCache.get(lus);
//            if (existingTimeHistories == null)
//            {
//                result = new ArrayList<ZoneTimeHistory>();
//                for (ZoneTimeHistory newTimeHistory : newTimeHistories)
//                {
//                    result.add(getOfficialZoneHistory(newTimeHistory));
//                }
//                zoneHistoryCache.put(lus, result);
//            }
//            else
//            {
//                result = existingTimeHistories;
//            }

//            return result;
        }
    }

//    private ZoneTimeHistory getOfficialZoneHistory(ZoneTimeHistory newTimeHistory)
//    {
//        String lus = newTimeHistory.getEquipmentColorLookupString();
//        synchronized (this)
//        {
//            ZoneTimeHistory zoneTimeHistory = zoneHistoryMap.get(lus);
//            if (zoneTimeHistory == null)
//            {
//                zoneHistoryMap.put(lus, newTimeHistory);
//                zoneTimeHistory = newTimeHistory;
//            }
//            return zoneTimeHistory;
//        }
//    }

    public synchronized void reset()
    {
        zoneHistoryCache.clear();
//        zoneHistoryMap.clear();
    }

    private DateRange setDateRangeToMidnight(DateRange dateRange)
    {
        // determine the start date
        long difference = dateRange.getEnd().getTime() - dateRange.getStart().getTime();
        int days = (int)(difference / ONE_DAY_MILLIS);

        Date end = getMidnightToday();
        Date start = getMidnight(days);

        return new DateRange(start, end);
    }

    private Date getMidnightToday()
    {
        return getMidnight(0);
    }

    private Date getMidnight(int daysAgo)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 0 - daysAgo);

        return cal.getTime();
    }
}

package com.controlj.addon.zonehistory.cache;

import com.controlj.addon.zonehistory.reports.ReportResultsData;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public enum ZoneHistoryCache
{
    CACHE;

    private final static long ONE_DAY_MILLIS = 1000L * 60 * 60 * 24;
    private Map<String, Map<DateRange, ReportResultsData>> zoneHistoryCache = new HashMap<String, Map<DateRange, ReportResultsData>>();

    @Nullable
    public ReportResultsData getCachedData(String lus, DateRange dateRange)
    {
        synchronized (this)
        {
            Map<DateRange, ReportResultsData> zoneHistories = zoneHistoryCache.get(lus);
            if (zoneHistories == null)
            {
                zoneHistoryCache.put(lus, new HashMap<DateRange, ReportResultsData>());
                return null;
            }

            if (zoneHistories.get(dateRange) == null)
                return null;

            return zoneHistories.get(dateRange);
        }
    }

    public void cacheResultsData(String lus, DateRange dateRange, ReportResultsData cachedResults)
    {
        synchronized (this)
        {
            trimCache(lus);
            Map<DateRange, ReportResultsData> currentZoneHistory = this.zoneHistoryCache.get(lus);
            if (currentZoneHistory == null)
                currentZoneHistory = new HashMap<DateRange, ReportResultsData>();

            // make sure the days for the DateRange are set to the midnights of their respective start and end dates
            DateRange midnightedDateRange = this.setDateRangeToMidnight(dateRange);
            currentZoneHistory.put(midnightedDateRange, cachedResults);
            zoneHistoryCache.put(lus, currentZoneHistory);
        }
    }

    public synchronized void reset()
    {
        zoneHistoryCache.clear();
    }

    private synchronized void trimCache(String lookup)
    {
//      get today at midnight
        Date today = getMidnightToday();

//      get date for yesterday, 1 week, 1 month ago
        Date yesterday = getMidnight(1);
        Date weekAgo = getMidnight(7);
        Date monthAgo = getMidnight(31);

//      create date range and set to midnights
        DateRange yesterdayRange = new DateRange(yesterday, today);
        DateRange weekRange = new DateRange(weekAgo, today);
        DateRange monthRange = new DateRange(monthAgo, today);

//      look at cache at the date ranges for the location and if any entries are present but not valid, remove them
        Map<DateRange, ReportResultsData> zoneHistories = zoneHistoryCache.get(lookup);
        if (zoneHistories == null)
            return;

        for (DateRange dateInCache : zoneHistories.keySet())
        {
            if (dateInCache.equals(yesterdayRange) || dateInCache.equals(weekRange) || dateInCache.equals(monthRange))
                continue;
            zoneHistories.remove(dateInCache);
            zoneHistoryCache.get(lookup).remove(dateInCache);
        }

        zoneHistoryCache.put(lookup, zoneHistories);
    }

    private DateRange setDateRangeToMidnight(DateRange dateRange)
    {
        // determine the start date
        long difference = dateRange.getEnd().getTime() - dateRange.getStart().getTime();
        int days = (int) (difference / ONE_DAY_MILLIS);

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

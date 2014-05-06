package com.controlj.addon.zonehistory.cache;

import com.controlj.addon.zonehistory.reports.ReportResultsData;
import com.controlj.addon.zonehistory.util.Logging;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
            Logging.LOGGER.println("Begin get cached data at "+lus+" for "+dateRange);

            Map<DateRange, ReportResultsData> zoneHistories = zoneHistoryCache.get(lus);
            if (zoneHistories == null)
            {
                Logging.LOGGER.println("No cache entry at " + lus);
                return null;
            }

//            Logging.LOGGER.println("Cache get range: " + dateRange.getStart() + " to " + dateRange.getEnd());

            if (zoneHistories.get(dateRange) == null)
            {
                Logging.LOGGER.println("Cache entry at"+lus+" has no data for range "+dateRange);
                return null;
            }

//            Logging.LOGGER.println("Cache Data FOUND for range");
            Logging.LOGGER.println("End get cached data at "+lus+" for "+dateRange);
            return zoneHistories.get(dateRange);
        }
    }

    public void cacheResultsData(String lus, DateRange dateRange, ReportResultsData cachedResults)
    {
        synchronized (this)
        {
            Logging.LOGGER.println("Begin cache results at "+lus+" for "+dateRange);
            trimCache(lus);

            try {
                Map<DateRange, ReportResultsData> currentZoneHistory = zoneHistoryCache.get(lus);
                if (currentZoneHistory == null)
                {
    //                Logging.LOGGER.println("Cache Storage at lookup(" + lus + ") is null; creating new map");
                    currentZoneHistory = new HashMap<DateRange, ReportResultsData>();
                }

                // make sure the days for the DateRange are set to the midnights of their respective start and end dates
                //DateRange midnightedDateRange = this.setDateRangeToMidnight(dateRange);
    //            Logging.LOGGER.println("Midnighted range storage....: " + midnightedDateRange.getStart() + " to " + midnightedDateRange.getEnd());
                currentZoneHistory.put(dateRange, cachedResults);
                zoneHistoryCache.put(lus, currentZoneHistory);
            }  catch (ConcurrentModificationException ex) {
                Logging.LOGGER.println("************ Syncronization problem detected in cacheResultsData ***********");
                throw ex;
            }
            Logging.LOGGER.println("End cache results at "+lus+" for "+dateRange);
        }
    }

    public synchronized void reset()
    {
        zoneHistoryCache.clear();
    }

    private void trimCache(String lookup)
    {
        try {
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

        Iterator<DateRange> it = zoneHistories.keySet().iterator();
        while (it.hasNext())
        {
            DateRange dateInCache = it.next();
            if (dateInCache.equals(yesterdayRange) || dateInCache.equals(weekRange) || dateInCache.equals(monthRange))
                continue;
            Logging.LOGGER.println("About to remove "+dateInCache+" from zoneHistories");
            it.remove();
            //Logging.LOGGER.println("About to remove "+dateInCache+" from zoneHistoryCache");
            //zoneHistoryCache.get(lookup).remove(dateInCache);
            //Logging.LOGGER.println("Successfully removed it");
        }

        zoneHistoryCache.put(lookup, zoneHistories);
        } catch (ConcurrentModificationException ex) {
            Logging.LOGGER.println("************ Syncronization problem detected in trimCache ***********");
            throw ex;
        }
    }

    private DateRange setDateRangeToMidnight(DateRange dateRange)
    {
        // determine the start date
        long difference = dateRange.getEnd().getTime() - dateRange.getStart().getTime();
        int days = (int) (difference / ONE_DAY_MILLIS);

        Date start = getMidnight(days);
        Date end = getMidnightToday();
//        Date end = getMidnight(1);

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

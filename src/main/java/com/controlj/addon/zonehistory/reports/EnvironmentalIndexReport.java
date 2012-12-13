package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.cache.DateRange;
import com.controlj.addon.zonehistory.cache.ZoneHistoryCache;
import com.controlj.addon.zonehistory.util.LocationUtilities;
import com.controlj.addon.zonehistory.util.Logging;
import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.aspect.AnalogTrendSource;
import com.controlj.green.addonsupport.access.aspect.AttachedEquipment;
import com.controlj.green.addonsupport.access.aspect.TrendSource;
import com.controlj.green.addonsupport.access.trend.TrendAnalogSample;
import com.controlj.green.addonsupport.access.trend.TrendData;
import com.controlj.green.addonsupport.access.trend.TrendRange;
import com.controlj.green.addonsupport.access.trend.TrendRangeFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class EnvironmentalIndexReport implements Report
{
    private final Date startDate, endDate;
    private final Location location;
    private final SystemConnection system;
    private final long DAY_MILLIS = 24 * 60 * 60 * 1000;  // precalculate the time for a day


    public EnvironmentalIndexReport(Date start, Date end, Location startingLocation, SystemConnection system)
    {
        this.startDate = start;
        this.endDate = end;
        this.location = startingLocation;
        this.system = system;
    }

    @Override
    public ReportResults runReport(final Collection<? extends TrendSource> sources) throws SystemException, ActionExecutionException
    {
        final TrendRange trendRange = TrendRangeFactory.byDateRange(startDate, endDate);
        return system.runReadAction(new ReadActionResult<ReportResults>()
        {
            @Override
            public ReportResults execute(@NotNull SystemAccess systemAccess) throws Exception
            {
                // this is to get the unoccupied times for use later to only use times that where within occupied times
                ReportResults<AnalogTrendSource> reportResults = new ReportResults<AnalogTrendSource>(location);
                DateRange dateRange = new DateRange(startDate, endDate);

                for (TrendSource iteratorSource : sources)
                {
                    try
                    {
                        AnalogTrendSource source = (AnalogTrendSource) iteratorSource;
                        Location sourceLocation = systemAccess.getTree(SystemTree.Geographic).resolve(source.getLocation().getTransientLookupString());
                        Location equipment = LocationUtilities.findMyEquipment(sourceLocation);
                        String persistentLookupString = equipment.getPersistentLookupString(true);

                        ReportResultsData cachedData = ZoneHistoryCache.CACHE.getCachedData(persistentLookupString, dateRange);
                        if (cachedData != null && cachedData.getAvgAreaForEI() > -1)
                        {
                            reportResults.addData(source, cachedData);
                            continue;
                        }

                        AttachedEquipment eqAspect = equipment.getAspect(AttachedEquipment.class);
                        if (!eqAspect.getDevice().isOutOfService())
                        {
                            EnvironmentalIndexProcessor processor = processTrendData(source, trendRange);

                            String displayPath = LocationUtilities.relativeDisplayPath(location, sourceLocation);
                            String transLookupPath = LocationUtilities.createTransientLookupPathString(sourceLocation);

                            if (cachedData == null)
                                cachedData = new ReportResultsData(processor.getTotalTime(), persistentLookupString,
                                        transLookupPath, displayPath, Collections.<EquipmentColor, Long>emptyMap(), 0, 0, 0);
                            cachedData.setAvgAreaForEI(processor.getAverageEI());
                            cachedData.setOccupiedTime(processor.getOccupiedTime());
                            cachedData.setArea(processor.getArea());

                            // avoid caching today's results
                            if (dateRange.getEnd().getTime() - dateRange.getStart().getTime() >= DAY_MILLIS)
                                ZoneHistoryCache.CACHE.cacheResultsData(persistentLookupString, dateRange, cachedData);

                            // add results to final report
                            reportResults.addData(source, cachedData);
                        }
                    }
                    catch (Exception e)
                    {
                        Logging.LOGGER.println("Error processing trend data");
                        e.printStackTrace(Logging.LOGGER);
                    }

                }

                return reportResults;
            }
        });
    }

    private EnvironmentalIndexProcessor processTrendData(AnalogTrendSource source, TrendRange range) throws TrendException
    {
        TrendData<TrendAnalogSample> tdata = source.getTrendData(range);
        return tdata.process(new EnvironmentalIndexProcessor());
    }
}

package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.cache.DateRange;
import com.controlj.addon.zonehistory.cache.GeoTreeSourceRetriever;
import com.controlj.addon.zonehistory.cache.ZoneHistoryCache;
import com.controlj.addon.zonehistory.cache.ZoneTimeHistory;
import com.controlj.addon.zonehistory.util.LocationUtilities;
import com.controlj.addon.zonehistory.util.Logging;
import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.aspect.AttachedEquipment;
import com.controlj.green.addonsupport.access.aspect.EquipmentColorTrendSource;
import com.controlj.green.addonsupport.access.trend.TrendData;
import com.controlj.green.addonsupport.access.trend.TrendEquipmentColorSample;
import com.controlj.green.addonsupport.access.trend.TrendRange;
import com.controlj.green.addonsupport.access.trend.TrendRangeFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SatisfactionReport implements Report
{
    private final Date startDate, endDate;
    private final Location location;
    private final SystemConnection system;
    private final List<DateRange> unoccupiedTimes;

    public SatisfactionReport(Date start, Date end, Location startingLocation, SystemConnection system)
    {
        this.startDate = start;
        this.endDate = end;
        this.location = startingLocation;
        this.system = system;
        this.unoccupiedTimes = new ArrayList<DateRange>();
    }

    @Override
    public ReportResults runReport() throws SystemException, ActionExecutionException
    {
        final TrendRange trendRange = TrendRangeFactory.byDateRange(startDate, endDate);

        return system.runReadAction(new ReadActionResult<ReportResults>()
        {
            @Override
            public ReportResults execute(@NotNull SystemAccess systemAccess) throws Exception
            {
//                StopWatch timer = new StopWatch();
//                timer.start();
                DateRange range = new DateRange(startDate, endDate);
                ReportResults<EquipmentColorTrendSource> reportResults = new ReportResults<EquipmentColorTrendSource>(location, ReportType.SatisfactionReport);

//              Pass in report results obj so we can check both the cache and the tree itself and only run the report on the sources which have no cached results
                new GeoTreeSourceRetriever(reportResults, range, ZoneHistoryCache.SATISFACTION).collectForColorSources();

                for (EquipmentColorTrendSource source : reportResults.getSources())
                {
//                    ReportResultsData cachedResults = reportResults.getDataFromSource(source);
                    ReportResultsData cachedResults = ZoneHistoryCache.SATISFACTION.getCachedData(source.getLocation(), range);
                    if (cachedResults != null)
                        continue;

                    try
                    {
                        Location equipmentColorLocation = systemAccess.getTree(SystemTree.Geographic).resolve(source.getLocation().getTransientLookupString());
                        Location equipment = LocationUtilities.findMyEquipment(equipmentColorLocation);
                        AttachedEquipment eqAspect = equipment.getAspect(AttachedEquipment.class);

                        if (!eqAspect.getDevice().isOutOfService())
                        {
                            if (SatisfactionProcessor.trace)
                                Logging.LOGGER.println("------ Processing " + equipment.getDisplayName());

//                                processTimer.resume();
                            SatisfactionProcessor processor = processTrendData(source, trendRange);
//                                processTimer.suspend();

                            cachedResults = new ReportResultsData(processor.getTotalTime(), location, equipmentColorLocation, processor.getColorMap());

                            // check unoccupiedTimes
                            checkDateRanges(processor.getUnoccupiedTimeList());

                            ZoneTimeHistory zoneTimeHistory = new ZoneTimeHistory(source.getLocation(), cachedResults, unoccupiedTimes);
//                            cachedResults = zoneTimeHistory.addResults(range, dataResults);
//                            zoneTimeHistory.addUnoccupiedTimes(unoccupiedTimes); // add zonehistory to cache
                            ZoneHistoryCache.SATISFACTION.addZoneTimeHistory(source.getLocation(), range, zoneTimeHistory);
                        }
                    }
                    catch (Exception e)
                    {
                        Logging.LOGGER.println("Error processing trend data");
                        e.printStackTrace(Logging.LOGGER);
                    }

                    reportResults.addData(source, cachedResults);
                }

                //Logging.LOGGER.println("Processing trend sources beneath '"+start.getDisplayPath()+"' took "+processTimer);
                //Logging.LOGGER.println();

                return reportResults;
            }
        });
    }

    private SatisfactionProcessor processTrendData(EquipmentColorTrendSource source, TrendRange range) throws TrendException
    {
        TrendData<TrendEquipmentColorSample> tdata = source.getTrendData(range);
        return tdata.process(new SatisfactionProcessor());
    }

    public List<DateRange> getUnoccupiedTimes()
    {
        return unoccupiedTimes;
    }

    private void checkDateRanges(List<DateRange> ranges)
    {
        for (DateRange dateRange : ranges)
        {
            if (!unoccupiedTimes.contains(dateRange))
                unoccupiedTimes.add(dateRange);
        }
    }
}

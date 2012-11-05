package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.cache.DateRange;
import com.controlj.addon.zonehistory.cache.GeoTreeSourceRetriever;
import com.controlj.addon.zonehistory.cache.ZoneHistoryCache;
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

import java.util.*;

public class SatisfactionReport implements Report
{
    private final Date startDate, endDate;
    private final Location location;
    private final SystemConnection system;
    private final List<DateRange> unoccupiedTimes;
    private final Map<EquipmentColor, Long> colorMap;
    private long activeHeatingTime, activeCoolingTime, operationalTime;

    public SatisfactionReport(Date start, Date end, Location startingLocation, SystemConnection system)
    {
        this.startDate = start;
        this.endDate = end;
        this.location = startingLocation;
        this.system = system;
        this.unoccupiedTimes = new ArrayList<DateRange>();
        this.colorMap = new HashMap<EquipmentColor, Long>();
        activeCoolingTime = 0; activeHeatingTime = 0; operationalTime = 0;
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
                new GeoTreeSourceRetriever(reportResults, range, ZoneHistoryCache.CACHE).collectForColorSources();

                for (EquipmentColorTrendSource source : reportResults.getSources())
                {
                    ReportResultsData cachedResults = ZoneHistoryCache.CACHE.getCachedData(source.getLocation().getPersistentLookupString(true), range);
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

                            SatisfactionProcessor processor = processTrendData(source, trendRange);
                            long operationalTime = processor.getOperationalTime();
                            long coolingTime = processor.getCoolingTime();
                            long heatingTime = processor.getHeatingTime();
                            colorMap.putAll(processor.getColorMap());

                            String displayPath = LocationUtilities.relativeDisplayPath(location, equipmentColorLocation);
                            String transLookupPath = LocationUtilities.createTransientLookupPathString(equipmentColorLocation);
                            String transLookup = equipment.getPersistentLookupString(true);

                            cachedResults = new ReportResultsData(processor.getTotalTime(), transLookup, transLookupPath, displayPath, processor.getColorMap(),
                                                                  operationalTime, coolingTime, heatingTime);

                            // check unoccupiedTimes
                            checkDateRanges(processor.getUnoccupiedTimeList());

                            // avoid caching today's results
                            long day = 24 * 60 * 60 * 1000;
                            if (range.getEnd().getTime() - range.getStart().getTime() > day)
                                ZoneHistoryCache.CACHE.cacheResultsData(transLookup, range, cachedResults);
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

    public long getActiveHeatingTime()
    {
        return activeHeatingTime;
    }

    public Map<EquipmentColor, Long> getColorMap()
    {
        return Collections.unmodifiableMap(colorMap);
    }

    public long getActiveCoolingTime()
    {
        return activeCoolingTime;
    }

    public long getOperationalTime()
    {
        return operationalTime;
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

package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.cache.DateRange;
import com.controlj.addon.zonehistory.cache.ZoneHistoryCache;
import com.controlj.addon.zonehistory.util.LocationUtilities;
import com.controlj.addon.zonehistory.util.Logging;
import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.aspect.AttachedEquipment;
import com.controlj.green.addonsupport.access.aspect.EquipmentColorTrendSource;
import com.controlj.green.addonsupport.access.aspect.TrendSource;
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
    private final Map<EquipmentColor, Long> colorMap;

    public SatisfactionReport(Date start, Date end, Location startingLocation, SystemConnection system)
    {
        this.startDate = start;
        this.endDate = end;
        this.location = startingLocation;
        this.system = system;
        this.colorMap = new HashMap<EquipmentColor, Long>();
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
                DateRange range = new DateRange(startDate, endDate);
                ReportResults<EquipmentColorTrendSource> reportResults = new ReportResults<EquipmentColorTrendSource>(location);

//                Logging.LOGGER.println("-------------------------\nSatisfaction report started\n---------------------------- ");
                for (TrendSource indexSource : sources)
                {
                    try
                    {
                        EquipmentColorTrendSource source = (EquipmentColorTrendSource)indexSource;
                        Location sourceLocation = systemAccess.getTree(SystemTree.Geographic).resolve(source.getLocation().getTransientLookupString());
                        Location equipment = LocationUtilities.findMyEquipment(sourceLocation);
                        String persistentLookupString = equipment.getPersistentLookupString(true);
                        String displayPath = LocationUtilities.relativeDisplayPath(location, equipment);

//                        Logging.LOGGER.println("Checking Cache for data - " + displayPath + "(" + persistentLookupString + ") at " + startDate + " to " + endDate);
                        ReportResultsData cachedResults = ZoneHistoryCache.CACHE.getCachedData(persistentLookupString, range);

                        if (cachedResults != null)
                        {
//                            Logging.LOGGER.println("Cached Results found! Continuing report");
                            cachedResults.setDisplayPath(displayPath);
                            reportResults.addData(source, cachedResults);
                            continue;
                        }

//                        Logging.LOGGER.println("Cached Results NOT found!");

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
                            String transLookupPath = LocationUtilities.createTransientLookupPathString(equipment);

                            cachedResults = new ReportResultsData(processor.getTotalTime(), persistentLookupString, transLookupPath, displayPath, processor.getColorMap(),
                                    operationalTime, coolingTime, heatingTime);

                            // caching results except for today's results
                            long day = 24 * 60 * 60 * 1000;
                            if (range.getEnd().getTime() - range.getStart().getTime() >= day)
                            {
//                                Logging.LOGGER.println("(SatisfactionReport) Adding to cache at " + persistentLookupString + " (" + range.getStart() + "to" + range.getEnd() + ")");
                                ZoneHistoryCache.CACHE.cacheResultsData(persistentLookupString, range, cachedResults);
                            }

                            reportResults.addData(source, cachedResults);
                        }
                    }
                    catch (Exception e)
                    {
                        Logging.LOGGER.println("Error processing trend data");
                        e.printStackTrace(Logging.LOGGER);
                    }

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
        return tdata.process(new SatisfactionProcessor(range.getStartDate(), range.getEndDate()));
    }
}

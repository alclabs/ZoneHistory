package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.cache.DateRange;
import com.controlj.addon.zonehistory.cache.GeoTreeSourceRetriever;
import com.controlj.addon.zonehistory.cache.ZoneHistoryCache;
import com.controlj.addon.zonehistory.util.LocationUtilities;
import com.controlj.addon.zonehistory.util.Logging;
import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.aspect.AnalogTrendSource;
import com.controlj.green.addonsupport.access.aspect.AttachedEquipment;
import com.controlj.green.addonsupport.access.trend.TrendAnalogSample;
import com.controlj.green.addonsupport.access.trend.TrendData;
import com.controlj.green.addonsupport.access.trend.TrendRange;
import com.controlj.green.addonsupport.access.trend.TrendRangeFactory;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EnvironmentalIndexReport implements Report
{
    private final Date startDate, endDate;
    private final Location location;
    private final SystemConnection system;

    public EnvironmentalIndexReport(Date start, Date end, Location startingLocation, SystemConnection system)
    {
        this.startDate = start;
        this.endDate = end;
        this.location = startingLocation;
        this.system = system;
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
                // this is to get the unoccupied times for use later to only use times that where within occupied times
                ReportResults<AnalogTrendSource> reportResults = new ReportResults<AnalogTrendSource>(location);
                DateRange dateRange = new DateRange(startDate, endDate);
                GeoTreeSourceRetriever retriever = new GeoTreeSourceRetriever(reportResults, dateRange, ZoneHistoryCache.CACHE);
                if (!retriever.hasEISources())
//                    throw new NoEnviroIndexSourcesException("No EI Trends named 'zn_enviro_indx_tn' at the current location.");
                      return reportResults;

                retriever.collectForAnalogSources();
                long day = 24 * 60 * 60 * 1000;

                for (AnalogTrendSource source : reportResults.getSources())
                {
                    ReportResultsData cachedData = ZoneHistoryCache.CACHE.getCachedData(source.getLocation().getPersistentLookupString(true), dateRange);

                    Location equipmentColorLocation = systemAccess.getTree(SystemTree.Geographic).resolve(source.getLocation().getTransientLookupString());
                    Location equipment = LocationUtilities.findMyEquipment(equipmentColorLocation);

                    try
                    {
                        AttachedEquipment eqAspect = equipment.getAspect(AttachedEquipment.class);
                        if (!eqAspect.getDevice().isOutOfService())
                        {
                            EnvironmentalIndexProcessor processor = processTrendData(source, trendRange);

                            String displayPath = LocationUtilities.relativeDisplayPath(location, equipmentColorLocation);
                            String transLookupPath = LocationUtilities.createTransientLookupPathString(equipmentColorLocation);
                            String transLookup = equipment.getPersistentLookupString(true);

                            if (cachedData == null)
                                cachedData = new ReportResultsData(processor.getTotalTime(), transLookup, transLookupPath, displayPath, Collections.<EquipmentColor, Long>emptyMap(),
                                                                  0, 0, 0);
                            cachedData.setAvgAreaForEI(processor.getAverageArea());
                            cachedData.setOccupiedTime(processor.getOccupiedTime());

                            // avoid caching today's results
                            if (dateRange.getEnd().getTime() - dateRange.getStart().getTime() > day)
                                ZoneHistoryCache.CACHE.cacheResultsData(source.getLocation().getPersistentLookupString(true), dateRange, cachedData);
                        }
                    }
                    catch (Exception e)
                    {
                        Logging.LOGGER.println("Error processing trend data");
                        e.printStackTrace(Logging.LOGGER);
                    }

                    reportResults.addData(source, cachedData);
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

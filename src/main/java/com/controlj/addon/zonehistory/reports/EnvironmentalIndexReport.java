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
                SatisfactionReport report = new SatisfactionReport(startDate, endDate, location, system);
                report.runReport();

                // this is to get the unoccupied times for use later to only use times that where within occupied times
                List<DateRange> unoccupiedRanges = report.getUnoccupiedTimes();
                Map<EquipmentColor, Long> colorMap = report.getColorMap();

                ReportResults<AnalogTrendSource> reportResults = new ReportResults<AnalogTrendSource>(location, ReportType.EnvironmentalIndexReport);
                DateRange dateRange = new DateRange(startDate, endDate);
                new GeoTreeSourceRetriever(reportResults, dateRange, ZoneHistoryCache.CACHE).collectForAnalogSources();

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
                            EnvironmentalIndexProcessor processor = processTrendData(source, trendRange, unoccupiedRanges);

                            String displayPath = LocationUtilities.relativeDisplayPath(location, equipmentColorLocation);
                            String transLookupPath = LocationUtilities.createTransientLookupPathString(equipmentColorLocation);
                            String transLookup = equipment.getPersistentLookupString(true);

                            if (cachedData == null)
                                cachedData = new ReportResultsData(processor.getTotalTime(), transLookup, transLookupPath, displayPath, colorMap,
                                                                  report.getOperationalTime(), report.getActiveCoolingTime(), report.getActiveHeatingTime());
                            cachedData.setAvgAreaForEI(processor.getAverageArea());
                            cachedData.setOccupiedTime(processor.getOccupiedTime());

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

    private EnvironmentalIndexProcessor processTrendData(AnalogTrendSource source, TrendRange range, List<DateRange> unoccupiedTimes) throws TrendException
    {
        TrendData<TrendAnalogSample> tdata = source.getTrendData(range);
        return tdata.process(new EnvironmentalIndexProcessor(unoccupiedTimes));
    }
}

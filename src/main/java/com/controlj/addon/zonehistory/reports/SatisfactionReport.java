package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.ColorTrendProcessor;
import com.controlj.addon.zonehistory.ColorTrendSource;
import com.controlj.addon.zonehistory.EnabledColorTrendWithSetpointAcceptor;
import com.controlj.addon.zonehistory.cache.DateRange;
import com.controlj.addon.zonehistory.cache.ZoneHistory;
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
import org.apache.commons.lang.time.StopWatch;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SatisfactionReport extends Report
{
    private final Date startDate, endDate;
    private final Location location;
    private final SystemConnection system;

    public SatisfactionReport(Date start, Date end, Location startingLocation, SystemConnection system)
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
        ReportResults results = system.runReadAction(new ReadActionResult<ReportResults>()
        {
            @Override
            public ReportResults execute(@NotNull SystemAccess systemAccess) throws Exception
            {
                StopWatch timer = new StopWatch();
                timer.start();
                Collection<ZoneHistory> zoneHistories = ZoneHistoryCache.INSTANCE.getDescendantZoneHistories(location);
                if (zoneHistories == null)
                {
                    Collection<EquipmentColorTrendSource> sources = location.find(EquipmentColorTrendSource.class, new EnabledColorTrendWithSetpointAcceptor());
                    ArrayList<ZoneHistory> newHistories = new ArrayList<ZoneHistory>();

                    for (EquipmentColorTrendSource source : sources)
                        newHistories.add(new ZoneHistory(source.getLocation()));

                    zoneHistories = ZoneHistoryCache.INSTANCE.addDescendantZoneHistories(location, newHistories);
                }
                timer.stop();
                //Logging.LOGGER.println("Search for trend sources beneath '"+start.getDisplayPath()+"' took "+timer);


                StopWatch processTimer = new StopWatch();
                processTimer.start();
                processTimer.suspend();

                boolean firstZone = true;

                Map<ColorTrendSource, Map<EquipmentColor, Long>> results = new HashMap<ColorTrendSource, Map<EquipmentColor, Long>>();
                for (ZoneHistory zoneHistory : zoneHistories)
                {
                    com.controlj.green.addonsupport.access.Location equipmentColorLocation = systemAccess.getTree(SystemTree.Geographic).resolve(zoneHistory.getEquipmentColorLookupString());
                    com.controlj.green.addonsupport.access.Location equipment = LocationUtilities.findMyEquipment(equipmentColorLocation);

                    if (firstZone)
                    {
                        //Logging.LOGGER.println("For "+equipment.getDisplayName()+", cache has "+zoneHistory.getCacheSize()+" entries and object is "+zoneHistory);
                        firstZone = false;
                    }

                    DateRange range = new DateRange(startDate, endDate);
                    Map<EquipmentColor, Long> colorMap = zoneHistory.getMapForDates(range);

                    if (colorMap == null)
                    {
                        EquipmentColorTrendSource source = equipmentColorLocation.getAspect(EquipmentColorTrendSource.class);
                        try
                        {
                            AttachedEquipment eqAspect = equipment.getAspect(AttachedEquipment.class);
                            if (!eqAspect.getDevice().isOutOfService())
                            {
                                if (ColorTrendProcessor.trace)
                                {
                                    Logging.LOGGER.println("------ Processing " + equipment.getDisplayName());
                                }
                                processTimer.resume();
                                ColorTrendProcessor processor = processTrendData(source, trendRange);
                                processTimer.suspend();

                                colorMap = zoneHistory.addMap(range, processor.getColorMap());
                            }
                        }
                        catch (Exception e)
                        {
                            Logging.LOGGER.println("Error processing trend data");
                            e.printStackTrace(Logging.LOGGER);
                        }
                    }
                    if (colorMap != null)
                    {
                        results.put(new ColorTrendSource(location, equipment), colorMap);
                    }
                }
                //Logging.LOGGER.println("Processing trend sources beneath '"+start.getDisplayPath()+"' took "+processTimer);
                //Logging.LOGGER.println();
                return new ReportResults(results);
            }
        });



        // must convert to JSON or return generic results object
        return results;
    }

    private ColorTrendProcessor processTrendData(EquipmentColorTrendSource source, TrendRange range) throws TrendException
    {
        TrendData<TrendEquipmentColorSample> tdata = source.getTrendData(range);
        return tdata.process(new ColorTrendProcessor());
    }
}
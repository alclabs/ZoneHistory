package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.cache.DateRange;
import com.controlj.addon.zonehistory.cache.ZoneHistory;
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
    private final static int BUCKETS = 10;

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
                // check the cache for this location (during the time range)
                Collection<ZoneHistory> zoneHistories = ZoneHistoryCache.INSTANCE.getDescendantZoneHistories(location);

                if (zoneHistories == null)
                {
                    Collection<AnalogTrendSource> sources = location.find(AnalogTrendSource.class, new AspectAcceptor<AnalogTrendSource>()
                    {
                        @Override
                        public boolean accept(@NotNull AnalogTrendSource source)
                        {
                            return source.getLocation().getReferenceName().equals("zn_enviro_indx_tn");
                        }
                    });

                    ArrayList<ZoneHistory> newHistories = new ArrayList<ZoneHistory>();

                    for (AnalogTrendSource source : sources)
                        newHistories.add(new ZoneHistory(source.getLocation()));

                    zoneHistories = ZoneHistoryCache.INSTANCE.addDescendantZoneHistories(location, newHistories);
                }

                // need to get EqColorTrendSource for the equip at the location.
                // if !null, process using the SatisfactionReportProcessor, get the unoccupiedRanges, and pass to the EIProcessor
                SatisfactionReport report = new SatisfactionReport(startDate, endDate, location.getParent(), system);
                report.runReport();
                List<DateRange> unoccupiedRanges = report.getUnoccupiedTimes();

                ReportResults reportResults = new ReportResults();

                for (ZoneHistory zoneHistory : zoneHistories)
                {
                    Location equipmentColorLocation = systemAccess.getTree(SystemTree.Geographic).resolve(zoneHistory.getEquipmentColorLookupString());
                    Location equipment = LocationUtilities.findMyEquipment(equipmentColorLocation);

                    AnalogTrendSource source = equipmentColorLocation.getAspect(AnalogTrendSource.class);
                    try
                    {
                        AttachedEquipment eqAspect = equipment.getAspect(AttachedEquipment.class);
                        if (!eqAspect.getDevice().isOutOfService())
                        {
                            EnvironmentalIndexProcessor processor = processTrendData(source, trendRange, unoccupiedRanges);

                            List<Long> buckets = processor.getPercentageBuckets();
                            ReportResultsData reportData = new ReportResultsData(processor.getOccupiedTime());

                            for (int i = 0; i < buckets.size(); i++)
                                reportData.addData(i, buckets.get(i));

                            reportResults.addData(source, reportData);
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

    private EnvironmentalIndexProcessor processTrendData(AnalogTrendSource source, TrendRange range, List<DateRange> unoccupiedTimes) throws TrendException
    {
        TrendData<TrendAnalogSample> tdata = source.getTrendData(range);
        return tdata.process(new EnvironmentalIndexProcessor(BUCKETS, unoccupiedTimes));
    }
}

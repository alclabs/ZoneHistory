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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class EnvironmentalIndexReport implements Report
{
    private final Date startDate, endDate;
    private final Location location;
    private final SystemConnection system;
    private final static int BUCKETS = 5;

    private final Collection<Bucket> buckets = new ArrayList<Bucket>();

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
                ReportResults<AnalogTrendSource> reportResults = new ReportResults<AnalogTrendSource>(location, ReportType.EnvironmentalIndexReport);
                new GeoTreeSourceRetriever(reportResults, new DateRange(startDate, endDate), ZoneHistoryCache.EI);

                for (AnalogTrendSource source : reportResults.getSources())
                {
                    ReportResultsData cachedResults = reportResults.getDataFromSource(source);

                    Location equipmentColorLocation = systemAccess.getTree(SystemTree.Geographic).resolve(cachedResults.getTransLookupString());
                    Location equipment = LocationUtilities.findMyEquipment(equipmentColorLocation);
//                    Collection<AnalogTrendSource> analogSources = equipmentColorLocation.find(AnalogTrendSource.class, Acceptors.aspectByName(AnalogTrendSource.class, "zn_enviro_indx_tn"));

                    try
                    {
                        AttachedEquipment eqAspect = equipment.getAspect(AttachedEquipment.class);
                        if (!eqAspect.getDevice().isOutOfService())
                        {
                            EnvironmentalIndexProcessor processor = processTrendData(source, trendRange, unoccupiedRanges);
                            ReportResultsData reportData = new ReportResultsData(processor.getOccupiedTime(), location, equipmentColorLocation);

                            List<Long> buckets = processor.getPercentageBuckets();
                            for (int i = 0; i < buckets.size(); i++)
                            {
                                if (buckets.get(i) > 0)
                                    reportData.addData(i, buckets.get(i));
                            }

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

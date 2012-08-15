package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.util.EnabledColorTrendWithSetpointAcceptor;
import com.controlj.addon.zonehistory.cache.ZoneHistory;
import com.controlj.addon.zonehistory.cache.ZoneHistoryCache;
import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.aspect.EquipmentColorTrendSource;
import com.controlj.green.addonsupport.access.trend.TrendRange;
import com.controlj.green.addonsupport.access.trend.TrendRangeFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

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
        return system.runReadAction(new ReadActionResult<SatisfactionReportResults>()
        {
            @Override
            public SatisfactionReportResults execute(@NotNull SystemAccess systemAccess) throws Exception
            {
                // check the cache for this location (during the time range)
                Collection<ZoneHistory> zoneHistories = ZoneHistoryCache.INSTANCE.getDescendantZoneHistories(location);

                if (zoneHistories == null)
                {
                    Collection<EquipmentColorTrendSource> sources = location.find(EquipmentColorTrendSource.class, new EnabledColorTrendWithSetpointAcceptor());
                    ArrayList<ZoneHistory> newHistories = new ArrayList<ZoneHistory>();

                    for (EquipmentColorTrendSource source : sources)
                        newHistories.add(new ZoneHistory(source.getLocation()));

                    zoneHistories = ZoneHistoryCache.INSTANCE.addDescendantZoneHistories(location, newHistories);
                }

                // run a satisfaction report for the same location and times to get unoccupied times


                // run an ei report using the unoccupied times found in the SatisfactionProcessor (modify to record the unoccupied times)


                SatisfactionReportResults results = null;


                return results;
            }
        });
    }
}

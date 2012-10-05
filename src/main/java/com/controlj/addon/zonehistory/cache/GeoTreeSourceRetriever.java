package com.controlj.addon.zonehistory.cache;

import com.controlj.addon.zonehistory.reports.ReportResults;
import com.controlj.addon.zonehistory.reports.ReportResultsData;
import com.controlj.addon.zonehistory.util.EnabledColorTrendWithSetpointAcceptor;
import com.controlj.green.addonsupport.access.AspectAcceptor;
import com.controlj.green.addonsupport.access.aspect.AnalogTrendSource;
import com.controlj.green.addonsupport.access.aspect.EquipmentColorTrendSource;
import com.controlj.green.addonsupport.access.aspect.TrendSource;
import com.controlj.green.addonsupport.access.util.Acceptors;

import java.util.Collection;

/*
* Purpose is to abstract away the cache and the searching for sources given an aspect or source type
* and return a collection of locations and results data (or nulls if no results exist for a particular test)
* */
public class GeoTreeSourceRetriever
{
    private final ReportResults reportResults;
    private final DateRange dateRange;
    private final ZoneHistoryCache cache;

    public GeoTreeSourceRetriever(ReportResults reportResults, DateRange range, ZoneHistoryCache cache)
    {
        this.reportResults = reportResults;
        this.dateRange = range;
        this.cache = cache;
    }

    private <T extends TrendSource> void collect(Class<T> aspectClass, AspectAcceptor<T> acceptor)
    {
//      Searches location for all sources that match the search parameters
        Collection<T> sources = reportResults.getAncestor().find(aspectClass, acceptor);

//      iterate through collection and check the caching system for any results data with the given DateRange, Report class, and Location
        for (T source : sources)
        {
            ReportResultsData cachedData = cache.getCachedData(source.getLocation().getPersistentLookupString(true), dateRange);
            reportResults.addData(source, cachedData);
        }
    }

    public void collectForAnalogSources()
    {
        collect(AnalogTrendSource.class, Acceptors.aspectByName(AnalogTrendSource.class, "zn_enviro_indx_tn"));
    }

    public void collectForColorSources()
    {
        collect(EquipmentColorTrendSource.class, new EnabledColorTrendWithSetpointAcceptor());
    }
}

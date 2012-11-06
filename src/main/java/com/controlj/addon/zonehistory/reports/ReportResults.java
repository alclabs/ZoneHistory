package com.controlj.addon.zonehistory.reports;


import com.controlj.green.addonsupport.access.Location;
import com.controlj.green.addonsupport.access.aspect.TrendSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ReportResults<T extends TrendSource>
{
    private final Map<T, ReportResultsData> data;
    private final Location ancestor;

    public ReportResults(Location ancestor)
    {
        data = new HashMap<T, ReportResultsData>();
        this.ancestor = ancestor;
    }

    public void addData(T source, ReportResultsData resultsData)
    {
        data.put(source, resultsData);
    }

    public Location getAncestor()
    {
        return ancestor;
    }

    public ReportResultsData getDataFromSource(T source) throws Exception
    {
        if (!data.containsKey(source))
            throw new Exception("Key not found");

        return data.get(source);
    }

    public ReportResultsData getAggregatedData() throws Exception
    {
        // Blank strings because this is only used for the main chart which doesn't need any locations in order to function
        ReportResultsData aggregatedData = new ReportResultsData(getTimeForAllResults(), "", "", "");

        for (T source : getSources())
        {
            ReportResultsData resultsData = getDataFromSource(source);

            // for each T in the data, iterate and combine
            Map<?, Long> sourceData = resultsData.getData();

            for (Object i : sourceData.keySet())
            {
                Long newTime = sourceData.get(i);
                Long currentTimeInResults = aggregatedData.getData().get(i) == null ? 0 : (Long) aggregatedData.getData().get(i);

                aggregatedData.addData(i, newTime + currentTimeInResults);
            }
        }

        return aggregatedData;
    }

    public Collection<? extends T> getSources()
    {
        return data.keySet();
    }

    private long getTimeForAllResults() throws Exception
    {
        long occupiedTime = 0;
        for (T source : getSources())
            occupiedTime += getDataFromSource(source).getTotalTime();

        return occupiedTime;
    }
}

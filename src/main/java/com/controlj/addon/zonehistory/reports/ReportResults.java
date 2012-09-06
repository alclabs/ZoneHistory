package com.controlj.addon.zonehistory.reports;


import com.controlj.green.addonsupport.access.aspect.TrendSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ReportResults
{
    private final Map<TrendSource, ReportResultsData> data;
    private final int buckets;

    public ReportResults(int b)
    {
        data = new HashMap<TrendSource, ReportResultsData>();
        buckets = b;
    }

    public ReportResults(Map<TrendSource, ReportResultsData> stuffs)
    {
        this.data = stuffs;
        buckets = stuffs.size();
    }

    public void addData(TrendSource source, ReportResultsData resultsData) throws Exception
    {
        data.put(source, resultsData);
    }

    public int getBuckets()
    {
        return buckets;
    }

    public ReportResultsData getDataFromSource(TrendSource source) throws Exception
    {
        if (!data.containsKey(source))
            throw new Exception("Key not found");

        return data.get(source);
    }

    public Collection<TrendSource> getSources()
    {
        return data.keySet();
    }

    public long getTimeForAllResults() throws Exception
    {
        long occupiedTime = 0;
        for (TrendSource source : getSources())
            occupiedTime += getDataFromSource(source).getTime();

        return occupiedTime;
    }
}

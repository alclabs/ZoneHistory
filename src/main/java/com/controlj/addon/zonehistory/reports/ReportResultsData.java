package com.controlj.addon.zonehistory.reports;

import java.util.HashMap;
import java.util.Map;

public class ReportResultsData
{
    private final Map<Integer, Long> data;
    private final long time;

    public ReportResultsData(long time)
    {
        this.data = new HashMap<Integer, Long>();
        this.time = time;
    }

    public ReportResultsData(Map<Integer, Long> data, long time)
    {
        this.data = data;
        this.time = time;
    }

    public long getTime()
    {
        return time;
    }

    public long getValue(int key) throws Exception
    {
        if (!data.containsKey(key))
            throw new Exception("Key not found");

        return data.get(key);
    }

    public void addData(int key, long value)
    {
        data.put(key, value);
    }

    public Map<Integer, Long> getData()
    {
        return data;
    }
}

package com.controlj.addon.zonehistory.reports;

import com.controlj.green.addonsupport.access.UnresolvableException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ReportResultsData<T>
{

    private final Map<T, Long> data;
    private final long time; // occupied time? - check to make sure it is
    private final String transientLookup, transientLookupPath, displayPath;

    public ReportResultsData(long time, String transLookup, String transLookupPath, String displayPath)
    {
        this.data = new HashMap<T, Long>();
        this.time = time;
        this.transientLookup = transLookup;
        this.transientLookupPath = transLookupPath;
        this.displayPath = displayPath;
    }

    public ReportResultsData(long time, String transLookup, String transLookupPath, String displayPath, Map<T, Long> rawData)
    {
        this(time, transLookup, transLookupPath, displayPath);
        data.putAll(rawData);
    }

    public String getDisplayPath() throws UnresolvableException
    {
        return displayPath;
    }

    public String getTransLookupString()
    {
        return transientLookup;
    }

    public String getTransLookupPath()
    {
        return transientLookupPath;
    }

    public long getTime()
    {
        if (time == 0)
        {
            long tempTime = 0;
            for (T key : this.data.keySet())
                tempTime += this.getData().get(key);

            return tempTime;
        }

        return time;
    }

    public long getValue(T key) throws Exception
    {
        if (!data.containsKey(key))
            throw new Exception("Key not found");

        return data.get(key);
    }

    public void addData(T key, Long value)
    {
        data.put(key, value);
    }

    public Map<T, Long> getData()
    {
        return Collections.unmodifiableMap(data);
    }
}

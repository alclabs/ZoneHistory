package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.util.LocationUtilities;
import com.controlj.green.addonsupport.access.Location;
import com.controlj.green.addonsupport.access.UnresolvableException;

import java.util.HashMap;
import java.util.Map;

public class ReportResultsData
{
    private final Map<Integer, Long> data;
    private final long time;
    private final String displayPath, transLookupString, transLookupPath;


    public ReportResultsData(long time, Location ancestor, Location descendant) throws UnresolvableException
    {
        this.data = new HashMap<Integer, Long>();
        this.time = time;
        this.displayPath = LocationUtilities.relativeDisplayPath(ancestor, descendant);
        this.transLookupString = descendant.getTransientLookupString();
        this.transLookupPath = LocationUtilities.createTransientLookupPathString(descendant);
    }

    public ReportResultsData(long time, Location ancestor, Location descendant, Map<Integer, Long> rawData) throws UnresolvableException
    {
        this.data = rawData;
        this.time = time;
        this.displayPath = LocationUtilities.relativeDisplayPath(ancestor, descendant);
        this.transLookupString = descendant.getTransientLookupString();
        this.transLookupPath = LocationUtilities.createTransientLookupPathString(descendant);
    }

    public String getDisplayPath()
    {
        return displayPath;
    }

    public String getTransLookupString()
    {
        return transLookupString;
    }

    public String getTransLookupPath()
    {
        return transLookupPath;
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

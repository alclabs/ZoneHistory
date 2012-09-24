package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.util.LocationUtilities;
import com.controlj.green.addonsupport.access.Location;
import com.controlj.green.addonsupport.access.UnresolvableException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ReportResultsData<T>
{

    private final Map<T, Long> data;
    private final long time; // occupied time? - check to make sure it is
    private final Location ancestor;
    private final Location descendant;

    public ReportResultsData(long time, Location ancestor, Location descendant)
    {
        this.data = new HashMap<T, Long>();
        this.time = time;
        this.ancestor = ancestor;
        this.descendant = descendant;
    }

    public ReportResultsData(long time, Location ancestor, Location descendant, Map<T, Long> rawData)
    {
        this(time, ancestor, descendant);
        data.putAll(rawData);
    }

    public String getDisplayPath() throws UnresolvableException
    {
        return LocationUtilities.relativeDisplayPath(ancestor, descendant);
    }

    public String getTransLookupString()
    {
        return descendant.getTransientLookupString();
    }

    public String getTransLookupPath()
    {
        return LocationUtilities.createTransientLookupPathString(descendant);
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
        // debug code to test eqColor coral
        /*if (data.containsKey(-1))
        {
            long temp = data.get(-1);
            data.put(13, temp);
            data.remove(-1);
        }*/
        return Collections.unmodifiableMap(data);
    }
}

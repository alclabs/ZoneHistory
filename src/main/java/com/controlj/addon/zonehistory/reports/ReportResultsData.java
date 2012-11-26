package com.controlj.addon.zonehistory.reports;

import com.controlj.green.addonsupport.access.UnresolvableException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ReportResultsData<T>
{

    private final Map<T, Long> data;
    private final long time; // occupied time? - check to make sure it is
    private long operationalTime, coolingTime, heatingTime, occupiedTime;
    private final String transientLookup, transientLookupPath, displayPath;
    private double avgAreaForEI, area;

    public ReportResultsData(long time, String transLookup, String transLookupPath, String displayPath)
    {
        this.time = time;
        this.transientLookup = transLookup;
        this.transientLookupPath = transLookupPath;
        this.displayPath = displayPath;

        this.operationalTime = 0;
        this.coolingTime = 0;
        this.heatingTime = 0;
        this.occupiedTime = 0;
        this.avgAreaForEI = -1;
        this.area = -1;

        this.data = new HashMap<T, Long>();
    }

    public ReportResultsData(long time, String transLookup, String transLookupPath, String displayPath, Map<T, Long> rawData)
    {
        this(time, transLookup, transLookupPath, displayPath);
        this.data.putAll(rawData);
    }

    public ReportResultsData(long time, String transLookup, String transLookupPath, String displayPath, Map<T, Long> rawData, long operationalTime, long activeCoolingTime, long activeHeatingTime)
    {
        this(time, transLookup, transLookupPath, displayPath, rawData);
        this.operationalTime = operationalTime;
        this.coolingTime = activeCoolingTime;
        this.heatingTime = activeHeatingTime;
    }

    public void setArea(double area)
    {
        this.area = area;
    }

    public long getOccupiedTime()
    {
        return occupiedTime;
    }

    public void setOccupiedTime(long occupiedTime)
    {
        this.occupiedTime = occupiedTime;
    }

    public double getAvgAreaForEI()
    {
        return avgAreaForEI;
    }

    public void setAvgAreaForEI(double avgAreaForEI)
    {
        this.avgAreaForEI = avgAreaForEI;
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

    public long getTotalTime()
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

    public long getActiveCoolingTime()
    {
        return coolingTime;
    }

    public long getActiveHeatingTime()
    {
        return heatingTime;
    }

    public long getOperationalTime()
    {
        return operationalTime;
    }

    public double getRawAreaForEICalculations()
    {
        return area;
    }
}

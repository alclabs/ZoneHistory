package com.controlj.addon.zonehistory.reports;

import com.controlj.green.addonsupport.access.aspect.TrendSource;

import java.util.HashMap;
import java.util.Map;

public class TrendSourceData
{
    private final Map<TrendSource, Map<Integer, Long>> data;

    public TrendSourceData()
    {
        data = new HashMap<TrendSource, Map<Integer, Long>>();
    }

    public void addData(TrendSource source, Map<Integer, Long> moreData)
    {
        data.put(source, moreData);
    }

    public Map<Integer, Long> getData(TrendSource source) throws Exception
    {
        if (!data.containsKey(source))
            throw new Exception("Key not found");

        return data.get(source);
    }
}

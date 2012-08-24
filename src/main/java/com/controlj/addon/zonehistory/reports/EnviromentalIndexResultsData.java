package com.controlj.addon.zonehistory.reports;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnviromentalIndexResultsData
{
    private final Map<Integer, Long> data;
    private final long unoccupiedTime;

    public EnviromentalIndexResultsData(List<Long> values, long unoccupiedTime)
    {
        data = new HashMap<Integer, Long>(values.size());
        for (int i = 0; i < values.size(); i++)
            this.data.put(i, values.get(i));

        this.unoccupiedTime = unoccupiedTime;
    }
}

package com.controlj.addon.zonehistory;

import com.controlj.green.addonsupport.access.EquipmentColor;
import com.controlj.green.addonsupport.access.trend.TrendEquipmentColorSample;
import com.controlj.green.addonsupport.access.trend.TrendProcessor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class ColorTrendProcessor implements TrendProcessor<TrendEquipmentColorSample>
{
    private EquipmentColor lastColor = EquipmentColor.UNKNOWN;
    private long lastTransitionTime;

    private Map<EquipmentColor, Long> colorMap = new HashMap<EquipmentColor, Long>();

    public Map<EquipmentColor, Long> getColorMap()
    {
        return colorMap;
    }

    public void processStart(Date startTime, TrendEquipmentColorSample startBookend)
    {
        lastTransitionTime = startTime.getTime();
        if (startBookend != null)
            lastColor = startBookend.value();
    }

    public void processData(TrendEquipmentColorSample sample)
    {
        long transitionTime = sample.getTimeInMillis();

        updateColorTotal(lastColor, transitionTime - lastTransitionTime);

        lastTransitionTime = transitionTime;
        lastColor = sample.value();
    }

    private void updateColorTotal(EquipmentColor color, long timeInterval)
    {
        Long time = colorMap.get(color);
        if (time == null)
            time = 0L;
        colorMap.put(color, time + timeInterval);
    }

    public void processHole(Date start, Date end)
    {
        // todo - lastTransision and start
        updateColorTotal(lastColor, start.getTime() - lastTransitionTime);
        lastTransitionTime = end.getTime();
        updateColorTotal(EquipmentColor.UNKNOWN, lastTransitionTime - start.getTime());
    }

    public void processEnd(Date endTime, TrendEquipmentColorSample endBookend)
    {
        updateColorTotal(lastColor, endTime.getTime() - lastTransitionTime);
    }
}

package com.controlj.addon.zonehistory;

import com.controlj.addon.zonehistory.util.Logging;
import com.controlj.green.addonsupport.access.EquipmentColor;
import com.controlj.green.addonsupport.access.trend.TrendEquipmentColorSample;
import com.controlj.green.addonsupport.access.trend.TrendProcessor;
import com.controlj.green.addonsupport.access.trend.TrendRange;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class ColorTrendProcessor implements TrendProcessor<TrendEquipmentColorSample>
{
    private EquipmentColor lastColor = EquipmentColor.UNKNOWN;
    private long lastTransitionTime;
    public static boolean trace = false;

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

        if (trace)
        {
            Logging.LOGGER.println("Process Start @"+startTime);
        }
    }

    public void processData(TrendEquipmentColorSample sample)
    {
        long transitionTime = sample.getTimeInMillis();

        updateColorTotal(lastColor, transitionTime - lastTransitionTime);

        lastTransitionTime = transitionTime;
        lastColor = sample.value();
        if (trace)
        {
            Logging.LOGGER.println("Processing data of "+lastColor+" @ "+sample.getTime());
        }
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
        // todo - lastTransition and start
        updateColorTotal(lastColor, start.getTime() - lastTransitionTime);
        lastTransitionTime = end.getTime();
        updateColorTotal(EquipmentColor.UNKNOWN, lastTransitionTime - start.getTime());
        lastColor = EquipmentColor.UNKNOWN;
        if (trace)
        {
            Logging.LOGGER.println("Processing hole from "+start+" to "+end);
        }
    }

    public void processEnd(@NotNull Date endTime, TrendEquipmentColorSample endBookend)
    {
        if (trace)
        {
            Logging.LOGGER.println("Processing end @"+endTime);
        }

        // If trending COV or server side color, we don't get any updates from the last transition till current time
        // treat it all as good
        updateColorTotal(lastColor, endTime.getTime() - lastTransitionTime);

        if (endBookend != null) // if there is data after this
        {
            //updateColorTotal(lastColor, endTime.getTime() - lastTransitionTime);
            if (trace) Logging.LOGGER.println("End has bookend at "+endBookend.getTime());
        } else
        {   // we don't really know the color because there are no more samples
            //updateColorTotal(EquipmentColor.UNKNOWN, endTime.getTime() - lastTransitionTime);
            if (trace) Logging.LOGGER.println("End has no bookend");
        }

    }

    public double getPercentCoverage()
    {
        double measuredTime = 0d;
        double unknownTime = 0d;
        for (EquipmentColor color : colorMap.keySet()) {
            if (color != EquipmentColor.UNKNOWN)
            {
                measuredTime += colorMap.get(color);
            } else
            {
                unknownTime += colorMap.get(color);
            }
        }
        return (measuredTime) / (measuredTime + unknownTime) * 100.0;
    }

    public long getTotalTime()
    {
        long totalTime = 0;
        for (Long time : colorMap.values()) {
            totalTime += time;
        }
        return totalTime;
    }
}

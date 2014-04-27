package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.util.ColorUtilities;
import com.controlj.addon.zonehistory.util.Logging;
import com.controlj.green.addonsupport.access.EquipmentColor;
import com.controlj.green.addonsupport.access.trend.TrendEquipmentColorSample;
import com.controlj.green.addonsupport.access.trend.TrendProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SatisfactionProcessor implements TrendProcessor<TrendEquipmentColorSample>
{
    private static final long FIFTEEN_MINUTES = 15 * 60 * 1000;
    private Map<EquipmentColor, Long> colorMap = new HashMap<EquipmentColor, Long>();

    private final Date desiredStart, desiredEnd;
    private EquipmentColor lastColor;
    private long lastTransitionTime, operatingTime, coolingTime, heatingTime;
    public static boolean trace = false;

    public SatisfactionProcessor(Date start, Date end)
    {
        this.desiredStart = start;
        this.desiredEnd   = end;
    }

    public void processStart(@NotNull Date startTime, TrendEquipmentColorSample startBookend)
    {
        if (trace)
            Logging.LOGGER.println("---------------Satisfaction Processor Start------------");

        operatingTime = 0;
        coolingTime = 0;
        heatingTime = 0;
        lastColor = startBookend != null ? startBookend.value() : EquipmentColor.UNKNOWN;
        lastTransitionTime = startTime.getTime();

        if (trace)
        {
            Logging.LOGGER.println("Process Start @" + startTime + "Current color is: " + lastColor);
            if (startBookend == null)
                Logging.LOGGER.println("Start bookend @" + startTime + " is null - default color Unknown used");
        }
    }

    public void processData(@NotNull TrendEquipmentColorSample sample)
    {
        long transitionTime = sample.getTimeInMillis();

        updateColorTotal(lastColor, transitionTime - lastTransitionTime);

        lastTransitionTime = transitionTime;
        lastColor = sample.value();

        if (trace)
            Logging.LOGGER.println("Processing data of " + lastColor + " @ " + sample.getTime());
    }

    public void processHole(@NotNull Date start, @NotNull Date end)
    {
        if (trace)
            Logging.LOGGER.println("Processing hole from " + start + " to " + end);

//        update the last color until the start of the hole
        updateColorTotal(lastColor, start.getTime() - lastTransitionTime);

        // Holes are 'unknown' values so we treat it as such and add the duration of the hole
        updateColorTotal(EquipmentColor.UNKNOWN, end.getTime() - start.getTime());

        if (end.getTime() - start.getTime() > FIFTEEN_MINUTES)
        {
            lastColor = EquipmentColor.UNKNOWN;
        }
        lastTransitionTime = end.getTime();
    }

    public void processEnd(@NotNull Date endTime, TrendEquipmentColorSample endBookend)
    {
        if (trace) Logging.LOGGER.println("Processing end @" + endTime);
/*
        if (lastTransitionTime == desiredStart.getTime())
        {
//            if the processor has just come from the process start method and has found no data points within the range, the data is unknown within the range
            colorMap.clear();
            updateColorTotal(EquipmentColor.UNKNOWN, desiredEnd.getTime() - desiredStart.getTime());
            if (trace) Logging.LOGGER.println("End reached with no data points inside. Entire range is Unknown.");
        }
        else if (endBookend == null)
        {
//            we don't really know the color because there are no more samples
            updateColorTotal(EquipmentColor.UNKNOWN, endTime.getTime() - lastTransitionTime);
            if (trace) Logging.LOGGER.println("End has no bookend; Unknown time added");
        }
        else
        {
        */
//            everything is ok, just add the duration since the lastTransitionTime to the lastColor known to us
            updateColorTotal(lastColor, endTime.getTime() - lastTransitionTime);
            if (trace) Logging.LOGGER.println("End has bookend at " + endBookend.getTime());
        //}
    }

    private void updateColorTotal(EquipmentColor color, long timeInterval)
    {
        // fix for 0 time. If there is no time added, g.raphael cannot render a slice of 0%
        if (timeInterval == 0)
            return;

        // check for color's current time, if it doesn't exist, create it
        Long time = colorMap.get(color);
        if (time == null)
            time = 0L;
        colorMap.put(color, time + timeInterval);

        // add time to other categories
        if (ColorUtilities.isOperating(color))
            operatingTime += timeInterval;
        if (ColorUtilities.isActiveHeating(color))
            heatingTime += timeInterval;
        else if (ColorUtilities.isActiveCooling(color))
            coolingTime += timeInterval;

        if (trace)
            Logging.LOGGER.println("Color added to Map: " + color.toString() + " time = " + (time + timeInterval));
    }

    public long getTotalTime()
    {
        long totalTime = 0;
        for (Long time : colorMap.values())
            totalTime += time;

        return totalTime;
    }

    public Map<EquipmentColor, Long> getColorMap()
    {
        return colorMap;
    }

    public long getOperatingTime()
    {
        return operatingTime;
    }

    public long getCoolingTime()
    {
        return coolingTime;
    }

    public long getHeatingTime()
    {
        return heatingTime;
    }

    // used in tests
    public double getPercentCoverage()
    {
        double measuredTime = 0d;
        double unknownTime = 0d;

        for (EquipmentColor color : colorMap.keySet())
        {
            if (color != EquipmentColor.UNKNOWN)
                measuredTime += colorMap.get(color);
            else
                unknownTime += colorMap.get(color);
        }

        return (measuredTime) / (measuredTime + unknownTime) * 100.0;
    }
}

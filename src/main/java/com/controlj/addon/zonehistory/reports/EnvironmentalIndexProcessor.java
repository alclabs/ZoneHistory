package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.util.Logging;
import com.controlj.green.addonsupport.access.trend.TrendAnalogSample;
import com.controlj.green.addonsupport.access.trend.TrendProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class EnvironmentalIndexProcessor implements TrendProcessor<TrendAnalogSample>
{
    private long totalTime, lastTransitionTime, occupiedTime;
    private double previousPoint, area, averageEI;
    public static boolean trace = false;

    public EnvironmentalIndexProcessor()
    {
        totalTime = 0;
        occupiedTime = 0;
        previousPoint = 0;
    }

    public long getTotalTime()
    {
        return totalTime;
    }

    public long getOccupiedTime()
    {
        return occupiedTime;
    }

    public double getArea()
    {
        return area;
    }

    public double getAverageEI()
    {
        return averageEI;
    }

    @Override
    public void processStart(@NotNull Date date, @Nullable TrendAnalogSample sample)
    {
        lastTransitionTime = date.getTime();
        if (sample != null)
            previousPoint = sample.doubleValue();
        if (trace)
            Logging.LOGGER.print("Start @ " + date.getTime() + " with sample value of: " + previousPoint + "\r\n");
    }

    @Override
    public void processData(@NotNull TrendAnalogSample sample)
    {
        long deltaTime = sample.getTimeInMillis() - lastTransitionTime;
        if (sample.doubleValue() > 0)
        {
//            Use midpoint between the two points as the height and multiply by the change in time to get the area. This is similar to the area for a trapezoid.
            area += ((previousPoint + sample.doubleValue()) / 2) * deltaTime;
            previousPoint = sample.doubleValue();
            occupiedTime += deltaTime;
        }

        totalTime += deltaTime;
        lastTransitionTime = sample.getTimeInMillis();

        if (trace)
            Logging.LOGGER.print("Data @ " + sample.getTime().getTime() + " with sample value of: " + previousPoint +
                    " Area at " + area + " Occupied Time: " + occupiedTime + " Average EI: " + area / occupiedTime + "\r\n");
    }

    @Override
    public void processEnd(@NotNull Date date, @Nullable TrendAnalogSample sample)
    {
        long deltaTime = date.getTime() - lastTransitionTime;
        // finalize the areas by adding up the last sample? is the bookend included? I believe no since it will be during or after the allotted time frame we're looking back through.
        if (sample != null && sample.doubleValue() > 0)
        {
            if (trace)
                Logging.LOGGER.print("End Sample not null" + " Average EI: " + area / occupiedTime + "\r\n");
            area += ((previousPoint + sample.doubleValue()) / 2) * deltaTime;
            occupiedTime += deltaTime;
        }
        else if (sample == null)
        {
//            occupiedTime += deltaTime;
//            area += previousPoint * deltaTime;
            if (trace)
                Logging.LOGGER.print("End Sample was null" + " Average EI: " + area / occupiedTime + "\r\n");
        }

        totalTime += deltaTime;

        // calculate average areas: total area / occupiedTime
        if (occupiedTime == 0)
            averageEI = 0.0;
        else
            averageEI = area / occupiedTime;
        if (trace)
            Logging.LOGGER.print("End @ " + date.getTime() + " with sample value of: " + previousPoint +
                    " Area at " + area + " Occupied Time: " + occupiedTime + " Average EI: " + area / occupiedTime + "\r\n");
    }

    @Override
    public void processHole(@NotNull Date start, @NotNull Date end)
    {
        if (trace)
            Logging.LOGGER.print("Hole @ " + start.getTime() + " Average EI: " + area / occupiedTime + "\r\n");

        totalTime += end.getTime() - start.getTime() + lastTransitionTime;
        lastTransitionTime = end.getTime() - start.getTime() + lastTransitionTime;
//        previousPoint = 0; // last point is at 0 so omit from totals
    }
}

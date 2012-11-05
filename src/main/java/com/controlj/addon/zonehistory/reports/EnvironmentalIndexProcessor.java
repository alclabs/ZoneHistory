package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.cache.DateRange;
import com.controlj.green.addonsupport.access.trend.TrendAnalogSample;
import com.controlj.green.addonsupport.access.trend.TrendProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.List;

public class EnvironmentalIndexProcessor implements TrendProcessor<TrendAnalogSample>
{
    private long totalTime, lastTransitionTime, unoccupiedTime, occupiedTime;
    private double previousPoint, area, averageArea;
    private final List<DateRange> unoccupiedTimes;

    public EnvironmentalIndexProcessor(List<DateRange> unoccupiedTimes)
    {
        this.unoccupiedTimes = unoccupiedTimes;
    }

    public long getTotalTime()
    {
        return totalTime;
    }

    public long getOccupiedTime()
    {
        return totalTime - unoccupiedTime;
    }

    public double getAverageArea()
    {
        return averageArea;
    }

    @Override
    public void processStart(@NotNull Date date, @Nullable TrendAnalogSample sample)
    {
        totalTime = 0;
        unoccupiedTime = 0;
        occupiedTime = 0;
        lastTransitionTime = date.getTime();

        if (sample != null)
            previousPoint = sample.doubleValue();
        else
            previousPoint = 0;
    }

    @Override
    public void processData(@NotNull TrendAnalogSample sample)
    {
        long deltaTime = sample.getTimeInMillis() - lastTransitionTime;
        if (!isUnoccupied(sample.getTime()))
        {
//            Use midpoint between the two points as the height and multiply by the change in time to get the area. This is similar to the area for a trapezoid.
            area += (previousPoint + sample.doubleValue() / 2) * deltaTime;
            occupiedTime += deltaTime;
        }

        totalTime += deltaTime;
        lastTransitionTime = sample.getTimeInMillis();
        previousPoint = sample.doubleValue();
    }

    @Override
    public void processEnd(@NotNull Date date, @Nullable TrendAnalogSample sample)
    {
        long deltaTime = date.getTime() - lastTransitionTime;
        // finalize the areas by adding up the last sample? is the bookend included? I believe no since it will be during or after the allotted time frame we're looking back through.
        if (sample != null && sample.doubleValue() > 0 && !isUnoccupied(sample.getTime()))
            occupiedTime += deltaTime;

        totalTime += deltaTime;

        // calculate average areas: total area / occupiedTime
        averageArea = area / occupiedTime;
    }

    @Override
    public void processHole(@NotNull Date start, @NotNull Date end)
    {
        lastTransitionTime = end.getTime();
        totalTime += end.getTime() - start.getTime();
        previousPoint = 0; // last point is at 0 so omit from totals

//        count as unoccupied? - yes. do not track values here
    }

    private boolean isUnoccupied(Date sampleTime)
    {
        // determine if a sample's date is found within the unoccupied times from the satisfaction report
        for (DateRange range : this.unoccupiedTimes)
        {
            if (range.isDateWithin(sampleTime))
                return true;
        }

        return false;
    }
}

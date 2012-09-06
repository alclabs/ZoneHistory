package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.cache.DateRange;
import com.controlj.green.addonsupport.access.trend.TrendAnalogSample;
import com.controlj.green.addonsupport.access.trend.TrendProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EnvironmentalIndexProcessor implements TrendProcessor<TrendAnalogSample>
{
    private long totalTime, lastTransitionTime, unoccupiedTime;
    private List<Long> percentageBuckets;
    private final List<DateRange> unoccupiedTimes;
    private final int buckets;

    public EnvironmentalIndexProcessor(int numberOfBuckets, List<DateRange> unoccupiedTimes)
    {
        this.percentageBuckets = new ArrayList<Long>(numberOfBuckets);
        for(int i = 0; i < numberOfBuckets; i++)
            percentageBuckets.add(i, 0l);

        this.unoccupiedTimes = unoccupiedTimes;
        this.buckets = numberOfBuckets;
    }

    public List<Long> getPercentageBuckets()
    {
        return this.percentageBuckets;
    }

    public long getTotalTime()
    {
        return totalTime;
    }

    public long getOccupiedTime()
    {
        return totalTime - unoccupiedTime;
    }

    public long getUnoccupiedTime()
    {
        return unoccupiedTime;
    }

    @Override
    public void processStart(@NotNull Date date, @Nullable TrendAnalogSample sample)
    {
        totalTime = 0;
        unoccupiedTime = 0;
        lastTransitionTime = date.getTime();

//        if (sample != null)
//            lastValue = sample.doubleValue();
    }

    @Override
    public void processData(@NotNull TrendAnalogSample sample)
    {
        placeIntoBucket(sample);
    }

    @Override
    public void processEnd(@NotNull Date date, @Nullable TrendAnalogSample sample)
    {
//        this.percentageBuckets.set(percentageBuckets.size() - 1, unoccupiedTime);

//        if (sample != null)
//            placeIntoBucket(sample);


    }

    @Override
    public void processHole(@NotNull Date start, @NotNull Date end)
    {
        lastTransitionTime = end.getTime();

//        dunno what to do if hole...count as unoccupied?
    }

    private void placeIntoBucket(TrendAnalogSample sample)
    {
        long duration = sample.getTimeInMillis() - lastTransitionTime;
        totalTime += duration;
        lastTransitionTime = sample.getTimeInMillis();

        if (isUnoccupied(sample.getTime()))
            unoccupiedTime += duration;
        else
        {
            int index = (int) (sample.doubleValue() / (100 / this.buckets)) - 1; // we only want unoccupied time in the very last spot

            try
            {
                long currentValue = percentageBuckets.get(index);
                percentageBuckets.set(index, currentValue + duration);
            }
            catch (IndexOutOfBoundsException e)
            {
                percentageBuckets.add(index, duration);
            }
        }
    }

    private boolean isUnoccupied(Date sampleTime)
    {
        // if the sample is found within the unoccupied times, return true
        for (DateRange range : this.unoccupiedTimes)
        {
            if (sampleTime.after(range.getStart()) && sampleTime.before(range.getEnd()))
                return true;
        }

        return false;
    }
}

package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.util.Logging;
import com.controlj.green.addonsupport.access.trend.TrendAnalogSample;
import com.controlj.green.addonsupport.access.trend.TrendProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class EnvironmentalIndexProcessor implements TrendProcessor<TrendAnalogSample>
{
    private long totalTime, occupiedTime, lastTransitionTime;
    private TrendAnalogSample previousSample, startBookend;
    private double area, averageEI;
    public static boolean trace = false;

    public EnvironmentalIndexProcessor()
    {
        totalTime = 0;
        occupiedTime = 0;
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
        startBookend = sample;
        if (trace)
            Logging.LOGGER.print("Start @ " + date.getTime() + " with sample value of: " + previousSample.doubleValue() + "\r\n");
    }

    private double interpolate(long t1, double val1, long t2, double val2, long t) {
        return ((val2 - val1) / (t2 - t1) * (t - t1)) + val1;
    }

    @Override
    public void processData(@NotNull TrendAnalogSample sample)
    {
        double lastSampleValue;

        long deltaTime = sample.getTimeInMillis() - lastTransitionTime;

        if (startBookend != null || previousSample != null)
        {
            if (startBookend != null) {
                if (startBookend.doubleValue() > 0)
                {
                    lastSampleValue = interpolate(  startBookend.getTimeInMillis(), startBookend.doubleValue(),
                                                    sample.getTimeInMillis(), sample.doubleValue(),
                                                    lastTransitionTime);
                } else {
                    lastSampleValue = startBookend.doubleValue();
                }
                startBookend = null;
            } else {
                lastSampleValue = previousSample.doubleValue();
            }

            if (lastSampleValue > 0)
            {
                if (sample.doubleValue() > 0)
                {
                    // Assume value linearly changed between last two samples
                    area += ((lastSampleValue + sample.doubleValue()) / 2) * deltaTime;
                } else
                {
                    // Assume value stayed the same until it went to 0
                    area += (lastSampleValue * deltaTime);
                }
                occupiedTime += deltaTime;
            }
        } else  // extend backwards to fill for holes or missing start data
        {
            if (sample.doubleValue() > 0)
            {
                area += sample.doubleValue() * deltaTime;
                occupiedTime += deltaTime;
            }
        }

        totalTime += deltaTime;
        previousSample = sample;
        lastTransitionTime = sample.getTimeInMillis();

        if (trace)
            Logging.LOGGER.print("Data @ " + sample.getTime().getTime() + " with sample value of: " + previousSample.doubleValue() +
                    " Area at " + area + " Occupied Time: " + occupiedTime + " Average EI: " + area / occupiedTime + "\r\n");
    }

    @Override
    public void processEnd(@NotNull Date date, @Nullable TrendAnalogSample endBookend)
    {
        // todo handle only start/end bookends
        //todo handle no bookends

        double lastSampleValue, finalSampleValue;
        long deltaTime = date.getTime() - lastTransitionTime;

        if (endBookend != null) {
            if (startBookend != null)   // there were no samples, only bookends
            {
                lastSampleValue = interpolate(  startBookend.getTimeInMillis(), startBookend.doubleValue(),
                                                endBookend.getTimeInMillis(), endBookend.doubleValue(),
                                                lastTransitionTime);
                // note that lastTransitionTime is already set to start time
            } else if (previousSample != null)  // there was a previous sample
            {
                lastSampleValue = previousSample.doubleValue();
            } else {                            // no start bookend or previous samples
                lastSampleValue = endBookend.doubleValue();    // extend last bookend backwards
            }

            finalSampleValue = interpolate( lastTransitionTime, lastSampleValue,
                                            endBookend.getTimeInMillis(), endBookend.doubleValue(),
                                            date.getTime());
        } else {    // no end bookend
            if (startBookend != null) {
                lastSampleValue = startBookend.doubleValue();
            } else if (previousSample != null){
                lastSampleValue = previousSample.doubleValue();
            } else {
                lastSampleValue = 0;
            }
            finalSampleValue = lastSampleValue;
        }

        if (lastSampleValue > 0)
        {
            if (finalSampleValue > 0)
            {
                // Assume value linearly changed between last two samples
                area += ((lastSampleValue + finalSampleValue) / 2) * deltaTime;
            } else
            {
                // Assume value stayed the same until it went to 0
                area += (lastSampleValue * deltaTime);
            }
            occupiedTime += deltaTime;
        }

        totalTime += deltaTime;

        // calculate average areas: total area / occupiedTime
        if (occupiedTime == 0)
            averageEI = 0.0;
        else
            averageEI = area / occupiedTime;
        if (trace)
            Logging.LOGGER.print("End @ " + date.getTime() + " with sample value of: " + previousSample.doubleValue() +
                    " Area at " + area + " Occupied Time: " + occupiedTime + " Average EI: " + area / occupiedTime + "\r\n");
    }

    @Override
    public void processHole(@NotNull Date start, @NotNull Date end)
    {
        double lastSampleValue;
        if (trace)
            Logging.LOGGER.print("Hole @ " + start.getTime() + " Average EI: " + (occupiedTime!=0 ? area / occupiedTime : 0) + "\r\n");

        long deltaTime = (start.getTime() - lastTransitionTime);

        if (startBookend != null || previousSample != null)
        {
            if (startBookend != null) {
                lastSampleValue = startBookend.doubleValue();
                startBookend = null;
            } else {
                lastSampleValue = previousSample.doubleValue();
            }

            if (lastSampleValue > 0)
            {
                    // Assume value stayed the same until it went to 0
                area += (lastSampleValue * deltaTime);
                occupiedTime += deltaTime;
            }
        }

        totalTime += (end.getTime() - lastTransitionTime);
        previousSample = null;
        startBookend = null;
        lastTransitionTime = end.getTime();
//        previousPoint = 0; // last point is at 0 so omit from totals
    }
}

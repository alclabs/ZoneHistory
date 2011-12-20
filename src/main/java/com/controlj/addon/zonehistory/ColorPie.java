package com.controlj.addon.zonehistory;

import com.controlj.green.addonsupport.access.EquipmentColor;

import java.util.Collection;
import java.util.Collections;

public class ColorPie
{
    private final Collection<ColorSlice> colorSlices;
    private long totalKnownTime = 0, totalTime = 0, satisfiedTime = 0;

    public ColorPie(Collection<ColorSlice> colorSlices)
    {
        this.colorSlices = Collections.unmodifiableCollection(colorSlices);
        computeTotalTimes();
    }

    public Collection<ColorSlice> getColorSlices()
    {
        return colorSlices;
    }

    public long getTotalKnownTime()
    {
        return totalKnownTime;
    }

    public long getTotalTime()
    {
        return totalTime;
    }

    private void computeTotalTimes()
    {

        for (ColorSlice slice : colorSlices) {
            long sliceTime = slice.getTimeInColor();
            EquipmentColor color = slice.getEquipmentColor();
            if (color != EquipmentColor.UNKNOWN)
            {
                totalKnownTime += sliceTime;
            }
            if (color == EquipmentColor.UNOCCUPIED || color == EquipmentColor.OPERATIONAL ||
                color == EquipmentColor.SPECKLED_GREEN || color == EquipmentColor.OCCUPIED)
            {
                satisfiedTime += sliceTime;
            }
            totalTime += sliceTime;
        }
    }

    public double getSlicePercent(ColorSlice slice)
    {
        return slice.getPercentTimeInColor(totalTime);
    }

    public double getSatisfaction()
    {
        if (totalKnownTime == 0)
        {
            return -1;  // marker value for all unknown
        }

        return satisfiedTime * 100.0 / totalKnownTime;
    }
}

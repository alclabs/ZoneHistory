package com.controlj.addon.zonehistory;

import com.controlj.green.addonsupport.access.EquipmentColor;

import java.util.Collection;
import java.util.Collections;

public class ColorPie
{
    private final Collection<ColorSlice> colorSlices;
    private final long totalTime;
    private final int numEquipment;

    public ColorPie(Collection<ColorSlice> colorSlices, long totalTime, int numEquipment)
    {
        this.colorSlices = Collections.unmodifiableCollection(colorSlices);
        this.totalTime = totalTime;
        this.numEquipment = numEquipment;
    }

    public Collection<ColorSlice> getColorSlices()
    {
        return colorSlices;
    }

    public long getTotalTime()
    {
        return totalTime;
    }

    public double getSlicePercent(ColorSlice slice)
    {
        return slice.getPercentTimeInColor(totalTime, numEquipment);
    }

    public double getSatisfaction()
    {
        double happyPercent = 0.0, unhappyPercent = 0.0;
        for (ColorSlice cs : colorSlices)
        {
            if (cs.getEquipmentColor() == EquipmentColor.UNOCCUPIED || cs.getEquipmentColor() == EquipmentColor.OPERATIONAL ||
                cs.getEquipmentColor() == EquipmentColor.SPECKLED_GREEN)
                happyPercent += cs.getPercentTimeInColor(totalTime, numEquipment);
            else
                unhappyPercent += cs.getPercentTimeInColor(totalTime, numEquipment);
        }

        return happyPercent / (happyPercent + unhappyPercent) * 100.0;
    }
}

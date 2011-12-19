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

    public long getTotalKnownTime()
    {
        long result = 0;
        for (ColorSlice slice : colorSlices) {
            if (!slice.getEquipmentColor().equals(EquipmentColor.UNKNOWN))
            {
                result += slice.getTimeInColor();
            }
        }
        return result;
    }

    public double getSlicePercent(ColorSlice slice)
    {
        return slice.getPercentTimeInColor(totalTime, numEquipment);
    }

    public double getSatisfaction()
    {
        long happyTime = 0, unhappyTime = 0;
        long knownTime = getTotalKnownTime();

        if (knownTime == 0)
        {
            return -1;  // marker value for all unknown
        }
        else for (ColorSlice cs : colorSlices)
        {
            if (cs.getEquipmentColor() == EquipmentColor.UNOCCUPIED || cs.getEquipmentColor() == EquipmentColor.OPERATIONAL ||
                cs.getEquipmentColor() == EquipmentColor.SPECKLED_GREEN || cs.getEquipmentColor()==EquipmentColor.OCCUPIED)
                happyTime += cs.getTimeInColor();
            else if (cs.getEquipmentColor() != EquipmentColor.UNKNOWN)
                unhappyTime += cs.getTimeInColor();
        }

        return happyTime * 100.0 / (happyTime + unhappyTime);
    }
}

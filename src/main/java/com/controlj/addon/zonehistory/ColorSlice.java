package com.controlj.addon.zonehistory;

import com.controlj.green.addonsupport.access.EquipmentColor;
import java.awt.*;

public class ColorSlice
{
    private EquipmentColor color;
    private long timeInColor;

    public ColorSlice(EquipmentColor color)
    {
        this.color = color;
        this.timeInColor = 0;
    }

    public EquipmentColor getEquipmentColor()
    {
        return color;
    }

    public Color getActualColor()
    {
        return color.getColor();
    }

    public double getPercentTimeInColor(long totalTime)
    {
        if (totalTime == 0)
        {
            return 100.0;
        }
        else
        {
            return (double)timeInColor / totalTime * 100.0;
        }
    }

    public long getTimeInColor()
    {
        return timeInColor;
    }

    public void addTimeInColor(long timeInColor)
    {
        this.timeInColor += timeInColor;
    }

    @Override
    public String toString()
    {
        return "ColorSlice{" +
                "color=" + color +
                ", timeInColor=" + timeInColor +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColorSlice that = (ColorSlice) o;

        if (timeInColor != that.timeInColor) return false;
        if (color != that.color) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = color != null ? color.hashCode() : 0;
        result = 31 * result + (int) (timeInColor ^ (timeInColor >>> 32));
        return result;
    }
}

package com.controlj.addon.zonehistory.charts;

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
        switch (color) {
            case HARDWARE_COMM_ERROR:
                return new Color(255,0,255);

            case UNOCCUPIED:
                return new Color(80,80,80);

            case HEATING_ALARM:
                return new Color(255,0,0);

            case MAXIMUM_HEATING:
                return new Color(0,0,255);

            case MODERATE_HEATING:
                return new Color(0,255,255);

            case OPERATIONAL:
                return new Color(0,255,0);

            case SPECKLED_GREEN:
                return new Color(144,238,144);

            case MODERATE_COOLING:
                return new Color(255,255,0);

            case MAXIMUM_COOLING:
                return new Color(255,136,0);

            case COOLING_ALARM:
                return new Color(255,0,0);

            case OCCUPIED:
                return new Color(255,255,255);

            case CORAL:
                return new Color(255,130,114);

            default:
                return color.getColor();
        }

    }

    public double getPercentTimeInColor(long totalTime)
    {
        return (double)timeInColor / totalTime * 100.0;
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

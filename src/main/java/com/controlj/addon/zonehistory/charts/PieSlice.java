package com.controlj.addon.zonehistory.charts;

import java.awt.*;

public class PieSlice
{
    private double percent;
    private Color color;

    public PieSlice(double percent, Color c)
    {
        this.percent = percent;
        this.color = c;
    }

    public double getPercent()
    {
        return percent;
    }

    public Color getColor()
    {
        return color;
    }

}

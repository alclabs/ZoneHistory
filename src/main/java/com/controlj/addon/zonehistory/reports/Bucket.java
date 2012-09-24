package com.controlj.addon.zonehistory.reports;

public class Bucket
{
    private final int low;
    private final int high;

    public Bucket(int low, int high)
    {
        this.low = low;
        this.high = high;
    }

    public boolean isWithinRange(int value)
    {
        return value >= this.low && value <= high;
    }
}

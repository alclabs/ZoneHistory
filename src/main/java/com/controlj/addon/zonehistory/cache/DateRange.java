package com.controlj.addon.zonehistory.cache;

import java.util.Date;

/**
 * Stores a range of Dates that are each rounded down to 15 minute ticks.
 * This is used as a key in a hashtable of cached results.
 */
public class DateRange {
    private static final long resolution = 15L * 60 * 1000;  // 15 minutes in milliseconds
    private Date start,end;

    public DateRange(Date start, Date end) {
        this.start = roundDown(start);
        this.end = roundDown(end);
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateRange dateRange = (DateRange) o;

        if (!end.equals(dateRange.end)) return false;
        if (!start.equals(dateRange.start)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        return result;
    }

    public static Date roundDown(Date date) {
        long time = date.getTime();
        long chunks = time / resolution;
        return new Date(resolution * chunks);
    }
}

package com.controlj.addon.zonehistory.cache;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Stores a range of Dates that are each rounded down to 15 minute ticks.
 * This is used as a key in a hashtable of cached results.
 */
public class DateRange
{
    private static final long resolution = 15L * 60 * 1000;  // 15 minutes in milliseconds
    private Date start, end;

    public DateRange(Date start, Date end)
    {
        this.start = roundDown(start);
        this.end = roundDown(end);
    }

    public DateRange(int daysAgoFromMidnightToday)
    {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        end = calendar.getTime();
        calendar.roll(Calendar.DAY_OF_YEAR, 0-daysAgoFromMidnightToday);
        start = calendar.getTime();
    }

    public Date getStart()
    {
        return start;
    }

    public Date getEnd()
    {
        return end;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateRange dateRange = (DateRange) o;
        return start.equals(dateRange.start) && end.equals(dateRange.end);
    }

    @Override
    public int hashCode()
    {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        return result;
    }

    public static Date roundDown(Date date)
    {
        long time = date.getTime();
        long chunks = time / resolution;
        return new Date(resolution * chunks);
    }

    public boolean isDateWithin(Date date)
    {
        return (start.before(date) && end.after(date)) || start.equals(date) || end.equals(date);
    }
}

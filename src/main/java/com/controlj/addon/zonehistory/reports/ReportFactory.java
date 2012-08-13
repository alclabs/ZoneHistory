package com.controlj.addon.zonehistory.reports;

import com.controlj.green.addonsupport.access.Location;
import com.controlj.green.addonsupport.access.SystemConnection;

import java.util.Date;

public class ReportFactory
{
    public Report createReport(String testToRun, Date startDate, Date endDate, Location location, SystemConnection connection)
    {
        if (testToRun.contains("environmental index"))
            return new EnvironmentalIndexReport(startDate, endDate, location, connection);
        else
            return new SatisfactionReport(startDate, endDate, location, connection);
    }
}

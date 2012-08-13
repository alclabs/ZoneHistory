package com.controlj.addon.zonehistory.reports;

import com.controlj.green.addonsupport.access.ActionExecutionException;
import com.controlj.green.addonsupport.access.Location;
import com.controlj.green.addonsupport.access.SystemConnection;
import com.controlj.green.addonsupport.access.SystemException;

import java.util.Date;

public class EnvironmentalIndexReport extends Report
{
    private final Date startDate, endDate;
    private final Location location;
    private final SystemConnection system;

    public EnvironmentalIndexReport(Date start, Date end, Location startingLocation, SystemConnection system)
    {
        this.startDate = start;
        this.endDate = end;
        this.location = startingLocation;
        this.system = system;
    }

    @Override
    public ReportResults runReport() throws SystemException, ActionExecutionException
    {




        return null;
    }
}

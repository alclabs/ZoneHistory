package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.charts.EnvironmentalIndexPieBuilder;
import com.controlj.addon.zonehistory.charts.PieChartJSONBuilder;
import com.controlj.addon.zonehistory.charts.SatisfactionPieBuilder;
import com.controlj.green.addonsupport.access.Location;
import com.controlj.green.addonsupport.access.SystemConnection;

import java.util.Date;

public class ReportFactory
{
    public Report createReport(String reportToRun, Date startDate, Date endDate, Location location, SystemConnection connection)
    {
//        if (reportToRun.contains("environmental index"))
//            return new EnvironmentalIndexReport(startDate, endDate, location, connection);
//        else
            return new SatisfactionReport(startDate, endDate, location, connection);
    }

    public PieChartJSONBuilder createPieChartJSONBuilder(Report report)
    {
        return report instanceof SatisfactionReport ? new SatisfactionPieBuilder() : new EnvironmentalIndexPieBuilder();
    }
}

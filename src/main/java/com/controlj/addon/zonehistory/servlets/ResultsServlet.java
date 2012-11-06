package com.controlj.addon.zonehistory.servlets;

import com.controlj.addon.zonehistory.charts.SatisfactionPieBuilder;
import com.controlj.addon.zonehistory.reports.*;
import com.controlj.addon.zonehistory.util.Logging;
import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.aspect.TrendSource;
import com.controlj.green.addonsupport.web.WebContext;
import com.controlj.green.addonsupport.web.WebContextFactory;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class ResultsServlet extends HttpServlet
{
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("application/json");
        final String loc = request.getParameter("location");
        final WebContext webContext = extractWebContext(request); // this will be null if NOT from an ViewBuilder include
        String daysString = request.getParameter("prevdays");
        final Date startDate = determineStartDate(daysString);
        final Date endDate = determineEndDate(daysString);
        final HttpServletResponse finalResponse = response;

        try
        {
            final SystemConnection systemConnection = DirectAccess.getDirectAccess().getUserSystemConnection(request);
            systemConnection.runReadAction(FieldAccessFactory.newFieldAccess(), new ReadAction()
            {
                @Override
                public void execute(@NotNull SystemAccess systemAccess) throws Exception
                {
                    Location location;
                    Tree geoTree = systemAccess.getTree(SystemTree.Geographic);
                    if (webContext != null)
                        location = webContext.getLinkedFromLocation(geoTree);
                    else
                        location = geoTree.resolve(loc);

                    SatisfactionReport satisfactionReport = new SatisfactionReport(startDate, endDate, location, systemConnection);
                    ReportResults satisfactionResults = satisfactionReport.runReport();

                    EnvironmentalIndexReport environmentalIndexReport = new EnvironmentalIndexReport(startDate, endDate, location, systemConnection);
                    ReportResults environmentalIndexReportResults = environmentalIndexReport.runReport();

                    // the results need to be combined to incorporate the EI results with the color results

                    ReportResults combinedResult = new ReportResults(location);
                    if (environmentalIndexReportResults.getSources().size() == 0) // there were no ei trends - use the other trends
                        combinedResult = satisfactionResults;
                    else
                    {
                        for (Object source : satisfactionResults.getSources())
                        {
                            ReportResultsData data = satisfactionResults.getDataFromSource((TrendSource) source);
                            String lus = data.getTransLookupString();

                            for (Object source2 : environmentalIndexReportResults.getSources())
                            {
                                ReportResultsData data2 = environmentalIndexReportResults.getDataFromSource((TrendSource) source2);
                                if (lus.equals(data2.getTransLookupString()))
                                    data.setAvgAreaForEI(data2.getAvgAreaForEI());
                            }

                            combinedResult.addData((TrendSource)source, data);
                        }

                    }

                    JSONObject results = new JSONObject();
                    results.put("mainChart", new SatisfactionPieBuilder().buildMainPieChart(combinedResult));

                    // if there is more than 1 eq, create table with their respective charts
                    if ((location.getType() == LocationType.Area || location.getType() == LocationType.System) && webContext == null)
                        results.put("table", new SatisfactionPieBuilder().buildAreaDetailsTable(combinedResult));

                    results.write(finalResponse.getWriter());
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace(Logging.LOGGER);

            String str = e.getCause().getMessage();
            if (e.getCause() instanceof NoEnviroIndexSourcesException)
                str = "Error!: " + str.substring(str.lastIndexOf(':') + 1) + "\nPick another location that has an environmental index microblock.";

            response.sendError(500, str);
        }

        finalResponse.flushBuffer();
    }

    private WebContext extractWebContext(HttpServletRequest request)
    {
        if (WebContextFactory.hasLinkedWebContext(request))
            return WebContextFactory.getLinkedWebContext(request);
        else
            return null;
    }

    private Date determineStartDate(String daysString)
    {
        int numberOfDays = getNumberOfDays(daysString);

        return getMidnight(numberOfDays);
    }

    private Date determineEndDate(String daysString)
    {
        int numberOfDays = getNumberOfDays(daysString);
        if (numberOfDays == 0)
            return new Date();

        return getMidnight(0);
    }

    private int getNumberOfDays(String daysString)
    {
        int numberOfDays = 0;
        try
        {
            numberOfDays = Integer.parseInt(daysString);
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace(Logging.LOGGER);
        }

        if (numberOfDays < 0)
            numberOfDays = 0;
        else if (numberOfDays > 31)
            numberOfDays = 31;

        return numberOfDays;
    }

    private Date getMidnight(int daysAgo)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 0 - daysAgo);

        return cal.getTime();
    }
}

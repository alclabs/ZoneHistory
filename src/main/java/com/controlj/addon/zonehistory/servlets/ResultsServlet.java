package com.controlj.addon.zonehistory.servlets;

import com.controlj.addon.zonehistory.reports.Report;
import com.controlj.addon.zonehistory.reports.ReportFactory;
import com.controlj.addon.zonehistory.reports.ReportResults;
import com.controlj.addon.zonehistory.util.Logging;
import com.controlj.green.addonsupport.access.*;
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
        final String action = request.getParameter("action");
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


                    Report report = new ReportFactory().createReport(action, startDate, endDate, location, systemConnection);
                    ReportResults reportResults = report.runReport();
                    JSONObject results = new JSONObject();
                    results.put("mainChart", reportResults.convertResultsToJSON());

                    // if there is more than 1 eq, create table with their respective charts
                    if ((location.getType() == LocationType.Area || location.getType() == LocationType.System) && webContext == null)
                        results.put("table", reportResults.createDetailsTable());

                    results.write(finalResponse.getWriter());
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace(Logging.LOGGER);
            response.getWriter().println(e.getMessage());
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
        {
            return new Date();
        }
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

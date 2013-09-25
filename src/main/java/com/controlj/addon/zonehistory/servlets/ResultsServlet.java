package com.controlj.addon.zonehistory.servlets;

import com.controlj.addon.zonehistory.cache.GeoTreeSourceRetriever;
import com.controlj.addon.zonehistory.charts.SatisfactionPieBuilder;
import com.controlj.addon.zonehistory.reports.*;
import com.controlj.addon.zonehistory.util.LocationUtilities;
import com.controlj.addon.zonehistory.util.Logging;
import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.aspect.AnalogTrendSource;
import com.controlj.green.addonsupport.access.aspect.EquipmentColorTrendSource;
import com.controlj.green.addonsupport.access.aspect.TrendSource;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class ResultsServlet extends HttpServlet
{
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("application/json");
        final String loc = request.getParameter("location");
        final Boolean isFromWeb = request.getParameter("isFromGfxPge").contains("true");
        final String daysString = request.getParameter("prevdays");
        final Date startDate = determineStartDate(daysString);
//        final Date startDate = getMidnight(2);
        final Date endDate = determineEndDate(daysString);
//        final Date endDate = getMidnight(1);

        final HttpServletResponse finalResponse = response;

//        Logging.LOGGER.println("Start date: " + startDate.toString());
//        Logging.LOGGER.println("End Date: " + endDate.toString());

        try
        {
            final SystemConnection systemConnection = DirectAccess.getDirectAccess().getUserSystemConnection(request);
            systemConnection.runReadAction(FieldAccessFactory.newFieldAccess(), new ReadAction()
            {
                @Override
                public void execute(@NotNull SystemAccess systemAccess) throws Exception
                {
                    Tree geoTree = systemAccess.getTree(SystemTree.Geographic);
                    Location location = loc.charAt(0)!='#' ? geoTree.resolve(loc) : systemAccess.resolveGQLPath(loc);
                    Location equipmentLoc = location.getType() == LocationType.Equipment ? LocationUtilities.findMyEquipment(location) : location;

//                    Logging.LOGGER.println("Location to search: " + location.getDisplayName());
//                    Logging.LOGGER.println("Equipment to use?:  " + equipmentLoc.getDisplayName());
                    // start logging times for performance
                    ReportResults reportResults = new ReportResults(location);
                    GeoTreeSourceRetriever retriever = new GeoTreeSourceRetriever(reportResults);

                    // split list into Color and Analog Lists
                    Collection<AnalogTrendSource> analogTrendSources = retriever.collectForAnalogSources();
                    Collection<EquipmentColorTrendSource> colorTrendSources = retriever.collectForColorSources();

                    SatisfactionReport satisfactionReport = new SatisfactionReport(startDate, endDate, location, systemConnection);
                    ReportResults satisfactionResults = satisfactionReport.runReport(colorTrendSources);

                    EnvironmentalIndexReport environmentalIndexReport = new EnvironmentalIndexReport(startDate, endDate, location, systemConnection);
                    ReportResults environmentalIndexReportResults = environmentalIndexReport.runReport(analogTrendSources);

                    /*
                    * Combine the color results (for the pie) with the EI results to get both ei and the colors from the pie chart
                    * */
                    ReportResults combinedResult = new ReportResults(equipmentLoc);
                    for (Object source : satisfactionResults.getSources())
                    {
                        ReportResultsData data = satisfactionResults.getDataFromSource((TrendSource) source);
                        String lus = data.getPersistentLookupString();

                        for (Object source2 : environmentalIndexReportResults.getSources())
                        {
                            ReportResultsData data2 = environmentalIndexReportResults.getDataFromSource((TrendSource) source2);
                            if (lus.equals(data2.getPersistentLookupString()))
                            {
                                data.setAvgAreaForEI(data2.getAvgAreaForEI());
                                data.setArea(data2.getRawAreaForEICalculations());
                                data.setOccupiedTime(data2.getOccupiedTime());
                            }
                        }

                        combinedResult.addData((TrendSource) source, data);
                    }

//                    }

                    JSONObject results = new JSONObject();
                    results.put("mainChart", new SatisfactionPieBuilder().buildMainPieChart(combinedResult));

                    if (isFromWeb)
                        results.put("table", new SatisfactionPieBuilder().buildTotalsForGraphicsPage(combinedResult));
                    else //if ((location.getType() == LocationType.Area || location.getType() == LocationType.System) && webContext == null)
                        results.put("table", new SatisfactionPieBuilder().buildAreaDetailsTable(combinedResult));

                    results.put("locationPath", getTreePath(location));

                    results.write(finalResponse.getWriter());
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace(Logging.LOGGER);
            response.sendError(500, e.getCause().getMessage());
        }

        finalResponse.flushBuffer();
    }

    private String getTreePath(@NotNull Location loc) throws UnresolvableException
    {
      StringBuffer result = new StringBuffer();
      while(loc.hasParent())
      {
         result.insert(0, loc.getTransientLookupString());
         result.insert(0, '/');
         loc = loc.getParent();
      }
      result.insert(0, loc.getTransientLookupString());
      result.insert(0, '/');
      return result.toString();
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

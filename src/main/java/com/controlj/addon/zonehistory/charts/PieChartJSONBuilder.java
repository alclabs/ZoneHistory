package com.controlj.addon.zonehistory.charts;

import com.controlj.addon.zonehistory.reports.ReportResults;
import com.controlj.addon.zonehistory.reports.ReportResultsData;
import com.controlj.green.addonsupport.access.aspect.TrendSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public abstract class PieChartJSONBuilder
{
    public abstract JSONObject makeSinglePieChart(Map<Integer, Long> data, int value, long time) throws JSONException;

    public JSONObject buildMainPieChart(ReportResults reportResults) throws Exception
    {
        // for all the source, combine data in each bucket
        Map<Integer, Long> rawData = combineResultsForAllSources(reportResults);
        return makeSinglePieChart(rawData, reportResults.getBuckets(), reportResults.getTimeForAllResults());
    }

    public JSONArray buildAreaDetailsTable(ReportResults reportResults) throws Exception
    {
        long time = reportResults.getTimeForAllResults();
        int buckets = reportResults.getBuckets();

        JSONArray tableData = new JSONArray();
        for (TrendSource source : reportResults.getSources())
        {
            ReportResultsData data = reportResults.getDataFromSource(source);
            JSONObject tableRow = new JSONObject();
            tableRow.put("eqDisplayName",     data.getDisplayPath());
            tableRow.put("eqTransLookup",     data.getTransLookupPath());
            tableRow.put("eqTransLookupPath", data.getTransLookupPath());
            tableRow.put("rowChart",          makeSinglePieChart(data.getData(), buckets, time)); // generate a pie per source data

            tableData.put(tableRow);
        }

        return tableData;
    }

    protected Map<Integer, Long> combineResultsForAllSources(ReportResults reportResults) throws Exception
    {
        Map<Integer, Long> results = new HashMap<Integer, Long>();
        for (TrendSource source : reportResults.getSources())
        {
            ReportResultsData resultsData = reportResults.getDataFromSource(source);
            Map<Integer, Long> data = resultsData.getData();

            for (Integer i : data.keySet())
            {
                long newTime = data.get(i);
                long currentTimeInResults = results.get(i) == null ? 0 : results.get(i);

                results.put(i, newTime + currentTimeInResults);
            }
        }

        return results;
    }

    protected JSONObject singleSliceObject(String labelForSlice, Color color, double percentage) throws JSONException
    {
        JSONObject obj = new JSONObject();
        obj.put("color", labelForSlice);
        obj.put("percent", percentage);
        obj.put("rgb-red", color.getRed());
        obj.put("rgb-green", color.getGreen());
        obj.put("rgb-blue", color.getBlue());

        return obj;
    }
}

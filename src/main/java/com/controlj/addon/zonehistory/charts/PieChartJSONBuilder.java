package com.controlj.addon.zonehistory.charts;

import com.controlj.addon.zonehistory.reports.ReportResults;
import com.controlj.addon.zonehistory.reports.ReportResultsData;
import com.controlj.green.addonsupport.access.aspect.TrendSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.Collection;

public abstract class PieChartJSONBuilder<T extends TrendSource>
{
    public abstract JSONObject makeSinglePieChart(ReportResultsData results) throws JSONException;

    public JSONObject buildMainPieChart(ReportResults reportResults) throws Exception
    {
        // for all the source, combine data in each bucket
        ReportResultsData combineData = reportResults.getAggregatedData();

        // sort here

        return makeSinglePieChart(combineData);
    }

    public JSONArray buildAreaDetailsTable(ReportResults reportResults) throws Exception
    {
        JSONArray tableData = new JSONArray();
        Collection<T> sources = reportResults.getSources();

        for (T source : sources)
        {
            ReportResultsData data = reportResults.getDataFromSource(source);
            JSONObject tableRow = new JSONObject();
            tableRow.put("eqDisplayName",     data.getDisplayPath());
            tableRow.put("eqTransLookup",     data.getTransLookupString());
            tableRow.put("eqTransLookupPath", data.getTransLookupPath());
            tableRow.put("rowChart",          makeSinglePieChart(data)); // generate a pie per source data

            tableData.put(tableRow);
        }

        return tableData;
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

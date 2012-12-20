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
        return makeSinglePieChart(combineData);
    }

    public JSONObject buildTotalsForGraphicsPage(ReportResults reportResults) throws Exception
    {
        JSONObject tableData = new JSONObject();
        Collection<T> sources = reportResults.getSources();

        long occupiedTime = 0, coolingTime = 0, heatingTime = 0, totalTime = 0, operationalTime = 0;
        double areaForEI = 0;

        for (T source : sources)
        {
            ReportResultsData data = reportResults.getDataFromSource(source);

            coolingTime     += data.getActiveCoolingTime();
            heatingTime     += data.getActiveHeatingTime();
            operationalTime += data.getOperationalTime();
            occupiedTime    += data.getOccupiedTime();
            areaForEI       += data.getRawAreaForEICalculations();
            totalTime       += data.getTotalTime();
        }

        tableData.put("operationalvalue", operationalTime);
        tableData.put("heatingvalue",     heatingTime);
        tableData.put("coolingvalue",     coolingTime);
        tableData.put("eivalue",          areaForEI == 0 ? 0 : areaForEI / occupiedTime);
        tableData.put("totalTime",        totalTime);

        return tableData;
    }


    public JSONArray buildAreaDetailsTable(ReportResults reportResults) throws Exception
    {
        JSONArray tableData = new JSONArray();
        Collection<T> sources = reportResults.getSources();

        for (T source : sources)
        {
            ReportResultsData data = reportResults.getDataFromSource(source);
            JSONObject tableRow = new JSONObject();
            tableRow.put("eqDisplayName",      data.getDisplayPath());
            tableRow.put("eqTransLookup",      data.getPersistentLookupString());
            tableRow.put("eqTransLookupPath",  data.getTransLookupPath());
            tableRow.put("operationalvalue",   data.getOperationalTime());
            tableRow.put("coolingvalue",       data.getActiveCoolingTime());
            tableRow.put("heatingvalue",       data.getActiveHeatingTime());
            tableRow.put("rowChart",           makeSinglePieChart(data)); // generate a pie per source data
            tableRow.put("eivalue",            data.getAvgAreaForEI());
            tableRow.put("totalTime",          data.getTotalTime());

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

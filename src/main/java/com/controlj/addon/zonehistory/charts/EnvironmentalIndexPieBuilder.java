package com.controlj.addon.zonehistory.charts;

import com.controlj.addon.zonehistory.reports.Report;
import com.controlj.addon.zonehistory.reports.ReportResults;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.Map;

public class EnvironmentalIndexPieBuilder extends PieChartJSONBuilder
{
    @Override
    public JSONObject buildPieChartJSON(Report report, ReportResults reportResults) throws Exception
    {
        Map<Integer, Long> rawData = super.combineResultsForAllSources(reportResults);

        // buckets are already present
        // so to build, we need to show the display ranges (0% to 9%, 10% to 19%, etc)
        // we also need to send the colors that are associated with the buckets - we want the gradual change from red to green (0% to 100%)
        //
        //
        JSONArray array = new JSONArray();
        for (Integer i : rawData.keySet())
        {
            // low end of range = i * 10
            int low = i * 10;
            // high end of range = low end + 9;
            int high = low + 9;
            String label = low + "% to " + high + "%";

            Color color = getPercentageColor(i / rawData.keySet().size());

            /*GET OCCUPIED TIME FROM SOMEWHERE USEFUL*/
            long occupiedTime = 1;
            double percentage = (double) rawData.get(i) / occupiedTime * 100.0;

            // Put into JSONObject
            array.put(super.singleSliceObject(label, color, percentage));
        }

        // CALCULATE TOTAL EI HERE
        double environmentalIndex = calculateEnironmentalIndex();

        JSONObject object = new JSONObject();
        object.put("colors", array);
        object.put("enviro_idx", environmentalIndex);

        return object;
    }

    @Override
    public JSONArray buildAreaDetailsTable(Report report, ReportResults reportResults) throws Exception
    {
        return null;
    }

    private Color getPercentageColor(int ratio)
    {
        int red = 255 - (255 * ratio);
        int green = 255 * ratio;

        return new Color(red, green, 0);
    }

    // not done
    private double calculateEnironmentalIndex()
    {
        return 0.0;
    }
}

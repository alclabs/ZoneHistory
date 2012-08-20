package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.charts.PieChart;
import com.controlj.addon.zonehistory.charts.PieSlice;
import com.controlj.green.addonsupport.access.aspect.AnalogTrendSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class EnvironmentalIndexReportResults implements ReportResults
{
    private final Map<AnalogTrendSource, List<Long>> sourceMap;

    public EnvironmentalIndexReportResults(Map<AnalogTrendSource, List<Long>> sourceTimes)
    {
        this.sourceMap = sourceTimes;
    }

    public PieChart buildTotalPie()
    {
        for (AnalogTrendSource source : sourceMap.keySet())
        {
            for (int i = 0; i < sourceMap.get(source).size() - 2; i++)
            {
                //
            }
        }

        return new PieChart();
    }

    private PieChart buildSourcePie(AnalogTrendSource source) throws Exception
    {
        if (!sourceMap.containsKey(source))
            throw new Exception("Key Not found");

        List<Long> sourceData = sourceMap.get(source);
        long occupiedTime = sourceData.get(sourceData.size() - 1);
        sourceData = sourceData.subList(0, sourceData.size() - 2);

        PieChart chart = new PieChart();
        for (int i = 0; i < sourceData.size(); i++)
        {
            long num = sourceData.get(i);
            PieSlice slice = new PieSlice(num / occupiedTime, getPercentageColor(i / sourceData.size()));
            chart.addSlice(slice);
        }

        return chart;
    }

    // returns a shade of red -> green based on the integer
    private Color getPercentageColor(int ratio)
    {
        int red = 255 - (255 * ratio);
        int green = 255 * ratio;

        return new Color(red, green, 0);
    }

    public JSONObject convertToJSON() throws JSONException
    {
        // use public to get total pie
        PieChart chart = this.buildTotalPie();
        // private to get internal pies

        return this.buildTotalPie().convertToJSON();
    }

    @Override
    public JSONArray createDetailsTable() throws JSONException
    {
        return null;
    }
}

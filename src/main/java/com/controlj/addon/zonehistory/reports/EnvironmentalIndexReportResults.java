package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.charts.PieChart;
import com.controlj.addon.zonehistory.charts.PieSlice;
import com.controlj.green.addonsupport.access.aspect.AnalogTrendSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnvironmentalIndexReportResults implements ReportResults
{
    private final Map<AnalogTrendSource, List<Long>> sourceMap;
    private final long occupiedTime;

    public EnvironmentalIndexReportResults(Map<AnalogTrendSource, List<Long>> sourceTimes, long occupiedTime)
    {
        this.sourceMap = sourceTimes;
        this.occupiedTime = occupiedTime;
    }

    @Override
    public JSONObject convertResultsToJSON() throws JSONException
    {
        return this.buildTotalPie().convertToJSON();
    }

    @Override
    public JSONArray createDetailsTable() throws JSONException
    {
        // build individual pie charts and place them into arrays
        return null;
    }

    // here to make the large (total) pie chart for all the sources in an area for the test; if only one source, no problem
    private PieChart buildTotalPie()
    {
        List<Long> totalPieData = new ArrayList<Long>();
        for (int i = 0; i < sourceMap.keySet().size(); i++)
            totalPieData.add(0l);

        // look at each arraylist and add up all the elements at the same indicies, place into slices
        for (AnalogTrendSource source : sourceMap.keySet())
            totalPieData = addTwoLists(totalPieData, sourceMap.get(source));

        return buildPieChart(totalPieData);
    }

    // take a list and add the contents to the other
    private List<Long> addTwoLists(List<Long> list1, List<Long> list2)
    {
        boolean isList1Longer = list1.size() >= list2.size();
        List<Long> longerList = isList1Longer ? list1 : list2;
        int smallerListSize = isList1Longer ? list2.size() : list1.size();

        for (int i = 0; i < smallerListSize; i++)
            longerList.set(i, list1.get(i) + list2.get(i));

        return longerList;
    }

    private PieChart buildPieChart(List<Long> data)
    {
        PieChart chart = new PieChart();
        for (int i = 0; i < data.size(); i++)
        {
            long num = data.get(i);
            PieSlice slice = new PieSlice(num / occupiedTime, getPercentageColor(i / data.size()));
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
}

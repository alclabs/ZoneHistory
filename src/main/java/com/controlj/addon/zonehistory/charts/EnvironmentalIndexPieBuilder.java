package com.controlj.addon.zonehistory.charts;

import com.controlj.addon.zonehistory.reports.ReportResultsData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.Map;

public class EnvironmentalIndexPieBuilder extends PieChartJSONBuilder
{

    @Override
    public JSONObject makeSinglePieChart(ReportResultsData reportResultsData) throws JSONException
    {
        // to build, we need to show the display ranges (0% to 9%, 10% to 19%, etc)
        // we also need to send the colors that are associated with the buckets - we want the gradual change from red to green (0% to 100%)
        JSONArray array = new JSONArray();
        Map rawData = reportResultsData.getData();
        long occupiedTime = reportResultsData.getTime();

        for (Object i : rawData.keySet())
        {
            Double value = (Double) rawData.get(i);
            Double ratio = value / occupiedTime;
//            Color color = getPercentageColor((double)i / buckets);
            Color color = Color.CYAN;
            double percentage = ratio * 100.0;
            array.put(super.singleSliceObject(
                    getLabel(value.intValue(), rawData.keySet().size()),
                    color,
                    percentage));
        }

        JSONObject object = new JSONObject();
        object.put("colors", array);
        object.put("percentlabel", calculateEnvironmentalIndex());

        return object;

    }

    private Color getPercentageColor(double ratio)
    {
        // use 4 or 5 colors to represent the EI
        // (From 0% to 100% EI - Red, Orange, Gray, Yellow, Green)

        if (ratio >= 0.75)
            return Color.GREEN;
        else if (ratio < 0.75 && ratio >= 0.5)
            return Color.YELLOW;
        else if (ratio < 0.5 && ratio >= 0.25)
            return Color.ORANGE;
        else if (ratio < 0.25)
            return Color.RED;
        else
            return Color.GRAY;
    }

//    Used to calculate ratio based on value - issue: produces red -> brownish -> green instead of easily distinguishable colors
//    private Color getPercentageColor(double ratio)
//    {
//        int red = (int) (255 - (255 * ratio));
//        int green = (int) (255 * ratio);
//
//        return new Color(red, green, 0);
//    }

    private String getLabel(int index, int buckets)
    {
        int bucketRatio = 100 / (buckets);
        int low = index * bucketRatio;
        int high = low + (bucketRatio);
        return low + "% to " + high + "%";
    }

    // not done
    private double calculateEnvironmentalIndex()
    {
        return -1.0; // average of EI
    }
}

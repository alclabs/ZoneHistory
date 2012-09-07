package com.controlj.addon.zonehistory.charts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.Map;

public class EnvironmentalIndexPieBuilder extends PieChartJSONBuilder
{

    @Override
    public JSONObject makeSinglePieChart(Map<Integer, Long> rawData, int buckets, long occupiedTime) throws JSONException
    {
        // to build, we need to show the display ranges (0% to 9%, 10% to 19%, etc)
        // we also need to send the colors that are associated with the buckets - we want the gradual change from red to green (0% to 100%)
        JSONArray array = new JSONArray();
        for (Integer i : rawData.keySet())
        {
            Color color = getPercentageColor((double)i / buckets);
            double percentage = (double) rawData.get(i) / occupiedTime * 100.0;
            array.put(super.singleSliceObject(getLabel(i, buckets), color, percentage));
        }

        JSONObject object = new JSONObject();
        object.put("colors", array);
        object.put("percentlabel", calculateEnvironmentalIndex());

        return object;

    }

    private Color getPercentageColor(double ratio)
    {
        int red = (int) (255 - (255 * ratio));
        int green = (int) (255 * ratio);

        return new Color(red, green, 0);
    }

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
        return 0.0; // average of EI
    }
}

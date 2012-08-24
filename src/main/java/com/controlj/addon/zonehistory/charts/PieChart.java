package com.controlj.addon.zonehistory.charts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PieChart
{
    private List<PieSlice> slices;

    public PieChart()
    {
        slices = new ArrayList<PieSlice>();
    }

    public PieChart(List<PieSlice> slices)
    {
        this.slices = slices;
    }

    public void addSlice(PieSlice singleSlice)
    {
        this.slices.add(singleSlice);
    }

    // convert to g.Raphael JSON
    public JSONObject convertToJSON() throws JSONException
    {
        JSONObject obj = new JSONObject();
        obj.put("satisfaction", 0.0); // will be changed to something else (doesn't matter right now)

        JSONArray array = new JSONArray();
        for (PieSlice cs : this.slices)
            array.put(singleResultIntoJSONObject(cs));

        obj.put("colors", array);

        return obj;
    }

    private JSONObject singleResultIntoJSONObject(PieSlice slice) throws JSONException
    {
        JSONObject obj = new JSONObject();
        obj.put("color", slice.getColor());
        obj.put("percent", slice.getPercent());
        obj.put("rgb-red", slice.getColor().getRed());
        obj.put("rgb-green", slice.getColor().getGreen());
        obj.put("rgb-blue", slice.getColor().getBlue());

        return obj;
    }
}

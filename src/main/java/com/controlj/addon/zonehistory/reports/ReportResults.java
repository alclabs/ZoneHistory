package com.controlj.addon.zonehistory.reports;


import com.controlj.green.addonsupport.access.aspect.TrendSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ReportResults
{
    private final Map<TrendSource, ReportResultsData> data;

    public ReportResults()
    {
        data = new HashMap<TrendSource, ReportResultsData>();
    }

    public ReportResults(Map<TrendSource, ReportResultsData> stuffs)
    {
        this.data = stuffs;
    }

    public void addData(TrendSource source, ReportResultsData resultsData) throws Exception
    {
       data.put(source, resultsData);
    }

    public ReportResultsData getDataFromSource(TrendSource source) throws Exception
    {
        if (!data.containsKey(source))
            throw new Exception("Key not found");

        return data.get(source);
    }

    public Collection<TrendSource> getSources()
    {
        return data.keySet();
    }


    // make pie chart creator that creates the pie based on the type of report that was run to determine how the pie is built

//    public JSONObject convertResultsToJSON() throws JSONException;
//    public JSONArray createDetailsTable() throws JSONException;
    // probably will need to include method to return raw percentages
    // in order to allow the chart creator easy access to the correct data
}

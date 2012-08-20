package com.controlj.addon.zonehistory.reports;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public interface ReportResults
{
    public JSONObject convertToJSON() throws JSONException;
    public JSONArray createDetailsTable() throws JSONException;
    // probably will need to include method to return raw percentages
    // in order to allow the chart creator easy access to the correct data
}

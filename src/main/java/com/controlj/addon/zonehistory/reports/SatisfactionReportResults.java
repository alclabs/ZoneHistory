/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2011 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)ColorTrendResults

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.zonehistory.reports;

import com.controlj.addon.zonehistory.charts.ColorPie;
import com.controlj.addon.zonehistory.charts.ColorSlice;
import com.controlj.addon.zonehistory.util.ColorTrendSource;
import com.controlj.green.addonsupport.access.EquipmentColor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class SatisfactionReportResults implements ReportResults
{
   private final Map<ColorTrendSource, Map<EquipmentColor, Long>> results;

   public SatisfactionReportResults(Map<ColorTrendSource, Map<EquipmentColor, Long>> results)
   {
      this.results = results;
   }

   public Set<ColorTrendSource> getSources()
   {
      return results.keySet();
   }

   public ColorPie getPieForSource(ColorTrendSource source)
   {
      Map<EquipmentColor, Long> equipmentColorMap = results.get(source);
      return computeResult(Collections.singleton(equipmentColorMap));
   }

    public ColorPie buildPieChart()
    {
        return computeResult(results.values());
    }

    @Override
    public JSONObject convertToJSON() throws JSONException
    {
        ColorPie hr = this.buildPieChart();
        return convertToJSON(hr);
    }

    private JSONObject convertToJSON(ColorPie hr) throws JSONException
    {
        JSONObject obj = new JSONObject();
        obj.put("satisfaction", hr.getSatisfaction());

        JSONArray array = new JSONArray();
        for (ColorSlice cs : hr.getColorSlices())
            array.put(singleResultIntoJSONObject(cs, hr.getSlicePercent(cs)));
        obj.put("colors", array);

        return obj;
    }

    @Override
    public JSONArray createDetailsTable() throws JSONException
    {
        // for each EquipmentColorTrendSource, get the results and compile into a JSON array
        JSONArray tableData = new JSONArray();

        for (ColorTrendSource cts : this.getSources())
        {
            JSONObject tableRow = new JSONObject();

            tableRow.put("eqDisplayName", cts.getDisplayPath());
            tableRow.put("eqTransLookup", cts.getTransientLookupString());
            tableRow.put("eqTransLookupPath", cts.getTransientLookupPathString());
            tableRow.put("rowChart", convertToJSON(this.getPieForSource(cts)));

            tableData.put(tableRow);
        }

        // Place objects into JSONArray which will be packaged into a single JSONObject called "Table"
        return tableData;
    }

    private ColorPie computeResult(Collection<Map<EquipmentColor, Long>> mapList)
   {
      Map<EquipmentColor, ColorSlice> results = new HashMap<EquipmentColor, ColorSlice>();
      for (Map<EquipmentColor, Long> colorMap : mapList)
      {
         for (Map.Entry<EquipmentColor, Long> colorEntry : colorMap.entrySet())
         {
            EquipmentColor color = colorEntry.getKey();
            ColorSlice slice = results.get(color);
            if (slice == null)
            {
               slice = new ColorSlice(color);
               results.put(color, slice);
            }
            // add to a slice's timeInColor
            slice.addTimeInColor(colorEntry.getValue());
         }
      }

      return new ColorPie(results.values());
   }

    private JSONObject singleResultIntoJSONObject(ColorSlice cs, double slicePercent) throws JSONException
    {
        JSONObject obj = new JSONObject();
        obj.put("color", cs.getEquipmentColor());
        obj.put("percent", slicePercent);
        obj.put("rgb-red", cs.getActualColor().getRed());
        obj.put("rgb-green", cs.getActualColor().getGreen());
        obj.put("rgb-blue", cs.getActualColor().getBlue());

        return obj;
    }
}
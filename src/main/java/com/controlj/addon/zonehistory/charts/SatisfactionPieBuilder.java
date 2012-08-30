package com.controlj.addon.zonehistory.charts;

import com.controlj.addon.zonehistory.reports.Report;
import com.controlj.addon.zonehistory.reports.ReportResults;
import com.controlj.addon.zonehistory.reports.ReportResultsData;
import com.controlj.green.addonsupport.access.EquipmentColor;
import com.controlj.green.addonsupport.access.aspect.TrendSource;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SatisfactionPieBuilder extends PieChartJSONBuilder
{
    @Override
    public JSONObject buildPieChartJSON(Report report, ReportResults reportResults) throws Exception
    {
        Map<Integer, Long> data = super.combineResultsForAllSources(reportResults);
        Map<EquipmentColor, Long> colorMap = new HashMap<EquipmentColor, Long>(data.size());
        long totalKnownTime = 0, totalTime = 0, satisfiedTime = 0;

        // convert ints to EquipmentColors
        // compute total times (found in ColorPie)
        for (Integer i : data.keySet())
        {
            EquipmentColor color = EquipmentColor.lookup(i);
            long colorTime = data.get(i);
            totalTime += colorTime;

            if (color != EquipmentColor.UNKNOWN)
                totalKnownTime += data.get(i);
            else if (color == EquipmentColor.UNOCCUPIED || color == EquipmentColor.OPERATIONAL ||
                    color == EquipmentColor.SPECKLED_GREEN || color == EquipmentColor.OCCUPIED)
                satisfiedTime += colorTime;

            colorMap.put(EquipmentColor.lookup(i), data.get(i));
        }

        // compute percentages per slice - make the pie itself
        JSONArray jsonPieArray = new JSONArray();
        for (EquipmentColor color : colorMap.keySet())
            jsonPieArray.put(super.singleSliceObject(color.toString(), getActualColor(color), colorMap.get(color)));

        // calculate satisfaction


        JSONObject object = new JSONObject();
        object.put("colors", jsonPieArray);
        object.put("satisfaction", getSatisfaction(totalKnownTime, satisfiedTime));

        return object;
    }

    private JSONObject buildIndividulaPie()
    {

    }

    @Override
    public JSONArray buildAreaTable(Report report, ReportResults reportResults) throws Exception
    {
       JSONArray tableData = new JSONArray();

      for (TrendSource cts : reportResults.getSources())
      {
          ReportResultsData data = reportResults.getDataFromSource(cts);
         JSONObject tableRow = new JSONObject();

         tableRow.put("eqDisplayName", data.getDisplayPath());
         tableRow.put("eqTransLookup", data.getTransLookupPath());
         tableRow.put("eqTransLookupPath", data.getTransLookupPath());
         tableRow.put("rowChart", toChartJSON(colorTrendResults.getPieForSource(cts))); // generate a pie per source data

         tableData.put(tableRow);
    }

    return tableData;
    }



   private JSONObject toChartJSON(ColorPie hr)
   {
      JSONObject obj = new JSONObject();
      obj.put("satisfaction", hr.getSatisfaction());

      JSONArray array = new JSONArray();
      for (ColorSlice cs : hr.getColorSlices())
         array.put(singleResultIntoJSONObject(cs, hr.getSlicePercent(cs)));
      obj.put("colors", array);

      return obj;
   }

    private double getSatisfaction(long totalKnownTime, long satisfiedTime)
    {
        if (totalKnownTime == 0)
            return -1;  // marker value for all unknown

        return satisfiedTime * 100.0 / totalKnownTime;
    }

    private Color getActualColor(EquipmentColor color)
    {
        switch (color) {
            case HARDWARE_COMM_ERROR:
                return new Color(255,0,255);

            case UNOCCUPIED:
                return new Color(80,80,80);

            case HEATING_ALARM:
                return new Color(255,0,0);

            case MAXIMUM_HEATING:
                return new Color(0,0,255);

            case MODERATE_HEATING:
                return new Color(0,255,255);

            case OPERATIONAL:
                return new Color(0,255,0);

            case SPECKLED_GREEN:
                return new Color(144,238,144);

            case MODERATE_COOLING:
                return new Color(255,255,0);

            case MAXIMUM_COOLING:
                return new Color(255,136,0);

            case COOLING_ALARM:
                return new Color(255,0,0);

            case OCCUPIED:
                return new Color(255,255,255);

            case CORAL:
                return new Color(255,130,114);

            default:
                return color.getColor();
        }

    }
}

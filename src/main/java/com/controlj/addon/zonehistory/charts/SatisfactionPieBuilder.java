package com.controlj.addon.zonehistory.charts;

import com.controlj.addon.zonehistory.reports.ReportResultsData;
import com.controlj.green.addonsupport.access.EquipmentColor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.Map;

public class SatisfactionPieBuilder extends PieChartJSONBuilder
{
    // makes pie charts based on single parts of data
    @Override
    public JSONObject makeSinglePieChart(ReportResultsData reportResultsData) throws JSONException
    {
        Map<EquipmentColor, Long> colorMap = reportResultsData.getData();
        long totalTime = 0;

        for (EquipmentColor color : colorMap.keySet())
        {
            long colorTime = colorMap.get(color);
            totalTime += colorTime;

//            if (color != EquipmentColor.UNKNOWN)
//                totalKnownTime += colorTime;
//
//            if (color == EquipmentColor.UNOCCUPIED || color == EquipmentColor.OPERATIONAL || color == EquipmentColor.SPECKLED_GREEN || color == EquipmentColor.OCCUPIED)
//                satisfiedTime += colorTime;
        }

        // compute percentages per slice - make the pie itself
        JSONArray jsonPieArray = new JSONArray();
        for (EquipmentColor color : colorMap.keySet())
        {
            String colorString = color.name();
            if (color == EquipmentColor.OPERATIONAL)
                colorString = "Ideal";  // this is actually a state of conditions being ideal (comfortable) whereas the concept of being operational is actually a state of having no errors.

            jsonPieArray.put(super.singleSliceObject(colorString, getActualColor(color), getPercentage(colorMap.get(color), totalTime)));
        }

        JSONObject object = new JSONObject();
        object.put("colors", jsonPieArray);
//        object.put("percentlabel", getSatisfaction(totalKnownTime, satisfiedTime));

        return object;
    }

//    private double getSatisfaction(long totalKnownTime, long satisfiedTime)
//    {
//        if (totalKnownTime == 0)
//            return -1;  // marker value for all unknown
//
//        return satisfiedTime * 100.0 / totalKnownTime;
//    }

    private Color getActualColor(EquipmentColor color)
    {
        switch (color)
        {
            case HARDWARE_COMM_ERROR:
                return new Color(255, 0, 255);

            case UNOCCUPIED:
                return color.getColor();
//                return new Color(80, 80, 80);

            case HEATING_ALARM:
                return new Color(255, 0, 0);

            case MAXIMUM_HEATING:
                return new Color(0, 0, 255);

            case MODERATE_HEATING:
                return new Color(0, 255, 255);

            case OPERATIONAL:
                return new Color(0, 255, 0);

            case SPECKLED_GREEN:
                return new Color(144, 238, 144);

            case MODERATE_COOLING:
                return new Color(255, 255, 0);

            case MAXIMUM_COOLING:
                return new Color(255, 136, 0);

            case COOLING_ALARM:
                return new Color(255, 0, 0);

            case OCCUPIED:
                return new Color(255, 255, 255);

            case CORAL:
                return new Color(255, 130, 114);

            default:
                return color.getColor();
        }

    }

    private double getPercentage(long colorTime, long totalTime)
    {
        return (double) colorTime / totalTime * 100.0;
    }
}

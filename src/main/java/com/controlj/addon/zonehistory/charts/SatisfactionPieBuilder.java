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
        }

        // compute percentages per slice - make the pie itself
        JSONArray jsonPieArray = new JSONArray();
        for (EquipmentColor color : colorMap.keySet())
        {
            /*
             * There are some inconsistencies with the Equipment Color concept and the way a user would interpret the titles.
             * EqColor.Operational implies that conditions are ideal or within the heating/cooling conditions where as operational
             * may mean Operational as in without error (but that includes heating, cooling, and ideal conditions).
             *
             * Speckled Green - it really means that the outside is cool enough to simply open the
             * vents to the outside air to assist with cooling the site/area/source/etc.
             *
             * Coral means essentially control program error and saying "Coral" would make no sense to a user
             */
            String colorString = color.name();
            if (color == EquipmentColor.OPERATIONAL)
                colorString = "Ideal";
            else if (color == EquipmentColor.CORAL)
                colorString = "Ctrl Prgm Error";
            else if (color == EquipmentColor.SPECKLED_GREEN)
                colorString = "Free Cooling";

            jsonPieArray.put(super.singleSliceObject(colorString, getActualColor(color), getPercentage(colorMap.get(color), totalTime)));
        }

        JSONObject object = new JSONObject();
        object.put("colors", jsonPieArray);

        return object;
    }

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

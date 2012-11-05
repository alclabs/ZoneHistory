package com.controlj.addon.zonehistory

import com.controlj.addon.zonehistory.charts.SatisfactionPieBuilder
import com.controlj.addon.zonehistory.reports.ReportResultsData
import com.controlj.green.addonsupport.access.EquipmentColor
import org.json.JSONObject
import spock.lang.Specification

class SatisfactionTests extends Specification
{
    def makeReportResultsData(long time)
    {
        new ReportResultsData(time, "", "", "")
    }

    def "Given many colors, get the correct total time" ()
    {
        given:
            ReportResultsData data = makeReportResultsData(300000L)
            data.addData(EquipmentColor.UNOCCUPIED, 50000L)
            data.addData(EquipmentColor.OPERATIONAL, 40000L)
            data.addData(EquipmentColor.MODERATE_COOLING, 10000L)
            data.addData(EquipmentColor.MODERATE_HEATING, 70000L)
            data.addData(EquipmentColor.SPECKLED_GREEN, 30000L)
            data.addData(EquipmentColor.COOLING_ALARM, 40000L)
            data.addData(EquipmentColor.HEATING_ALARM, 20000L)
            data.addData(EquipmentColor.UNKNOWN, 40000L)

        when: "pie calculates satisfaction"
            long totalTime = data.getTotalTime();
        then:
            totalTime == 300000L
    }

    def "Satisfaction report with all colors"()
    {
        given:
            ReportResultsData data = makeReportResultsData(300000L)
            data.addData(EquipmentColor.UNOCCUPIED, 50000L)
            data.addData(EquipmentColor.OPERATIONAL, 40000L)
            data.addData(EquipmentColor.MODERATE_COOLING, 10000L)
            data.addData(EquipmentColor.MODERATE_HEATING, 70000L)
            data.addData(EquipmentColor.SPECKLED_GREEN, 30000L)
            data.addData(EquipmentColor.COOLING_ALARM, 40000L)
            data.addData(EquipmentColor.HEATING_ALARM, 20000L)
            data.addData(EquipmentColor.UNKNOWN, 40000L)

            SatisfactionPieBuilder builder = new SatisfactionPieBuilder();

        when: "pie calculates satisfaction"
            JSONObject object = builder.makeSinglePieChart(data);

        then:
            double satisfaction = Double.parseDouble(object.get("percentlabel").toString())
            Math.round(satisfaction) == 46
    }

    def "No satisfaction colors"()
    {
        given:
            ReportResultsData data = makeReportResultsData(18000)
            data.addData(EquipmentColor.MODERATE_COOLING, 1000L)
            data.addData(EquipmentColor.MODERATE_HEATING, 7000L)
            data.addData(EquipmentColor.COOLING_ALARM, 4000L)
            data.addData(EquipmentColor.HEATING_ALARM, 2000L)
            data.addData(EquipmentColor.UNKNOWN, 4000L)

            SatisfactionPieBuilder builder = new SatisfactionPieBuilder();
        when:
            JSONObject object = builder.makeSinglePieChart(data);
        then:
            double satisfaction = Double.parseDouble(object.get("percentlabel").toString())
            Math.round(satisfaction) == 0
    }

    def "only satisfaction colors"()
    {
        given:
            ReportResultsData data = makeReportResultsData(18000)
            data.addData(EquipmentColor.UNOCCUPIED, 50000L)
            data.addData(EquipmentColor.OPERATIONAL, 40000L)
            data.addData(EquipmentColor.SPECKLED_GREEN, 30000L)
            data.addData(EquipmentColor.OCCUPIED, 50000L)

            SatisfactionPieBuilder builder = new SatisfactionPieBuilder();

        when:
            JSONObject object = builder.makeSinglePieChart(data);
        then:
            double satisfaction = Double.parseDouble(object.get("percentlabel").toString())
            Math.round(satisfaction) == 100.0
    }
}

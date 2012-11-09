package com.controlj.addon.zonehistory

import com.controlj.addon.zonehistory.reports.ReportResultsData
import com.controlj.green.addonsupport.access.EquipmentColor
import spock.lang.Specification

class CalculationTests extends Specification
{
    def makeReportResultsData(long time)
    {
        new ReportResultsData(time, "", "", "")
    }

    def "Given many colors, get the correct total time" ()
    {
        given:
            ReportResultsData data = makeReportResultsData(0L)
            data.addData(EquipmentColor.UNOCCUPIED, 50000L)
            data.addData(EquipmentColor.OPERATIONAL, 40000L)
            data.addData(EquipmentColor.MODERATE_COOLING, 10000L)
            data.addData(EquipmentColor.MODERATE_HEATING, 70000L)
            data.addData(EquipmentColor.SPECKLED_GREEN, 30000L)
            data.addData(EquipmentColor.COOLING_ALARM, 40000L)
            data.addData(EquipmentColor.HEATING_ALARM, 20000L)
            data.addData(EquipmentColor.UNKNOWN, 40000L)

        when: "pie calculates total time"
            long totalTime = data.getTotalTime();
        then:
            totalTime == 300000L
    }
}

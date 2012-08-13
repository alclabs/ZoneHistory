package com.controlj.addon.zonehistory

import static com.controlj.green.addonsupport.access.EquipmentColor.*
import spock.lang.Specification
import com.controlj.green.addonsupport.access.EquipmentColor
import com.controlj.addon.zonehistory.reports.ReportResults

class ReportResultsTest extends Specification
{
    def slice(EquipmentColor color, long time)
    {
        def slice = new ColorSlice(color)
        slice.timeInColor = time
        slice
    }
    def source() { new ColorTrendSource("", "")}

    def "colorReport computeResults - check ColorPie contents"()
    {
        given:
            def map1 = [(OCCUPIED) : 1000L, (OPERATIONAL) : 2000L]
            def map2 = [(MODERATE_COOLING) : 2000L, (UNOCCUPIED) : 5000L]
            def map3 = [(OPERATIONAL) : 1000L, (MODERATE_HEATING) : 3000L]
            def sourcesMap = [(source()) : map1, (source()) : map2, (source()) : map3]
            ReportResults results = new ReportResults(sourcesMap)

        when: "computeResults executes"
            def pie = results.getTotalPie()
            List slices = pie.colorSlices.sort { ColorSlice a, b ->  a.equipmentColor.name() <=> b.equipmentColor.name() }
        then: "compare results"
            slices == [slice(MODERATE_COOLING, 2000), slice(MODERATE_HEATING, 3000), slice(OCCUPIED, 1000),
                       slice(OPERATIONAL, 3000), slice(UNOCCUPIED, 5000)]
            pie.totalKnownTime == 14000L
    }

    def "Test if computeResults gives empty list"()
    {
        given:
            def map1 = [(OCCUPIED) : 1000L, (OPERATIONAL) : 2000L]
            def map2 = [(MODERATE_COOLING) : 2000L, (UNOCCUPIED) : 5000L]
            def map3 = [(OPERATIONAL) : 1000L, (MODERATE_HEATING) : 3000L]
            def sourcesMap = [(source()) : map1, (source()) : map2, (source()) : map3]
            ReportResults results = new ReportResults(sourcesMap)

        when: "execution of computeResults"
            def pie = results.getTotalPie()
        then: "test if resulting list of colors is empty"
            !pie.colorSlices.isEmpty()
    }
}

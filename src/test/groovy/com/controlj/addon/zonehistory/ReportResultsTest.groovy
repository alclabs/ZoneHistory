package com.controlj.addon.zonehistory

import com.controlj.addon.zonehistory.charts.SatisfactionPieBuilder
import com.controlj.addon.zonehistory.reports.ReportResults
import com.controlj.addon.zonehistory.reports.ReportResultsData
import com.controlj.green.addonsupport.access.Location
import com.controlj.green.addonsupport.access.aspect.TrendSource
import spock.lang.Specification
import static com.controlj.green.addonsupport.access.EquipmentColor.*

class ReportResultsTest<T extends TrendSource> extends Specification
{
    def pieBuilder() { new SatisfactionPieBuilder() }
    def reportResults() { new ReportResults(Mock(Location)) }

    def "compute aggregated data"()
    {
        given:
            def ReportResultsData resultsData1 = new ReportResultsData(3000L, "1", "1", "1")
            resultsData1.addData(OCCUPIED, 1000L)
            resultsData1.addData(OPERATIONAL, 2000L)

            def ReportResultsData resultsData2 = new ReportResultsData(7000L, "2", "2", "2")
            resultsData2.addData(MODERATE_COOLING, 2000L)
            resultsData2.addData(UNOCCUPIED, 5000L)


            def ReportResultsData resultsData3 = new ReportResultsData(4000L, "3", "3", "3")
            resultsData3.addData(OPERATIONAL, 5000L)
            resultsData3.addData(MODERATE_HEATING, 3000L)

            def reportResultsObj = reportResults();
            reportResultsObj.addData(Mock(TrendSource), resultsData1)
            reportResultsObj.addData(Mock(TrendSource), resultsData2)
            reportResultsObj.addData(Mock(TrendSource), resultsData3)


        when: "test aggregating data"
            def pieBuilder = pieBuilder();
            def aggregatedData = reportResultsObj.getAggregatedData()
            def mainPie = pieBuilder.buildMainPieChart(reportResultsObj)
        then: "compare results"
            aggregatedData.getValue(MODERATE_COOLING) == 2000L &&
            aggregatedData.getValue(MODERATE_HEATING) == 3000L &&
            aggregatedData.getValue(OCCUPIED)         == 1000L &&
            aggregatedData.getValue(OPERATIONAL)      == 7000L &&
            aggregatedData.getValue(UNOCCUPIED)       == 5000L
    }
}

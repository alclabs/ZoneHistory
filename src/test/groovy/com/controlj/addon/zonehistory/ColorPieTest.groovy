package com.controlj.addon.zonehistory

import com.controlj.green.addonsupport.access.EquipmentColor
import spock.lang.Specification

class ColorPieTest extends Specification
{
    def slice(EquipmentColor equipmentColor, long time)
    {
//        ColorSlice colorSlice = new ColorSlice(equipmentColor)
        colorSlice.timeInColor = time
        colorSlice
    }

//    long totalSliceTime(List<ColorSlice> slices)
    {
        slices.inject(0) { acc, slice ->
            acc + slice.timeInColor
        }
    }
    
    def "Given all colors, get the correct satisfaction for the color pie" ()
    {
        given:
//            ColorSlice cs1 = slice(UNOCCUPIED, 50000L)
//            ColorSlice cs2 = slice(OPERATIONAL, 40000L)
//            ColorSlice cs3 = slice(MODERATE_COOLING, 10000L)
//            ColorSlice cs4 = slice(MODERATE_HEATING, 70000L)
//            ColorSlice cs5 = slice(SPECKLED_GREEN, 30000L)
//            ColorSlice cs6 = slice(COOLING_ALARM, 40000L)
//            ColorSlice cs7 = slice(HEATING_ALARM, 20000L)
//            ColorSlice cs8 = slice(UNKNOWN, 40000L)
//
//            ColorPie pie = new ColorPie([cs1, cs2, cs3, cs4, cs5, cs6, cs7, cs8]) // five hours, five equipment
            long totalTime = totalSliceTime([cs1, cs2, cs3, cs4, cs5, cs6, cs7, cs8])
            long totalKnownTime = totalTime - cs8.getTimeInColor();

        when: "pie calculates satisfaction"
            double satisfaction = pie.getSatisfaction();
        then:
            Math.round(satisfaction) == Math.round(100 * totalSliceTime([cs1, cs2, cs5]) / totalKnownTime)

        expect:
            Math.round(pie.getSlicePercent(cs1)) == Math.round(cs1.getTimeInColor() *100 / totalTime )
    }

    def "all unknown"()
    {
        given:
//            ColorSlice cs1 = slice(UNKNOWN, 40000L)
//
//            ColorPie pie = new ColorPie([cs1])

            double satisfaction = pie.getSatisfaction();

        expect:
            satisfaction == -1
            Math.round(pie.getSlicePercent(cs1)) == 100
    }


    def "No satisfaction colors"()
    {
        given:
//            ColorSlice cs1 = slice(MODERATE_COOLING, 50000L)
//            ColorSlice cs2 = slice(MODERATE_HEATING, 40000L)
//            ColorSlice cs3 = slice(COOLING_ALARM, 10000L)
//            ColorSlice cs4 = slice(HEATING_ALARM, 70000L)
//            ColorSlice cs5 = slice(UNKNOWN, 30000L)
//
//            ColorPie pie = new ColorPie([cs1, cs2, cs3, cs4, cs5])
        when:
            double satisfaction = pie.getSatisfaction()
        then:
            Math.round(satisfaction) == 0
    }

    def "only satisfaction colors"()
    {
        given:
//            ColorSlice cs1 = slice(UNOCCUPIED, 50000L)
//            ColorSlice cs2 = slice(OPERATIONAL, 40000L)
//            ColorSlice cs3 = slice(SPECKLED_GREEN, 20000L)
//            ColorSlice cs4 = slice(OCCUPIED, 50000L)
//            ColorPie pie = new ColorPie([cs1, cs2, cs3, cs4])
        when:
            double satisfaction = pie.getSatisfaction()
        then:
            Math.round(satisfaction) == 100.0
    }


    def "given null ColorSlice collection"()
    {
//        when:
//            new ColorPie(null)
//        then:
//            thrown(NullPointerException)
    }
}

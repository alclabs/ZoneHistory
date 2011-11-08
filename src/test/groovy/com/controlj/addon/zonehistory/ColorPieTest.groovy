package com.controlj.addon.zonehistory

import com.controlj.green.addonsupport.access.EquipmentColor
import spock.lang.Specification
import static com.controlj.green.addonsupport.access.EquipmentColor.*

class ColorPieTest extends Specification
{
    def slice(EquipmentColor equipmentColor, long time)
    {
        ColorSlice colorSlice = new ColorSlice(equipmentColor)
        colorSlice.timeInColor = time
        colorSlice
    }
    
    def "Given all colors, get the correct satisfaction for the color pie" ()
    {
        given:
            ColorSlice cs1 = slice(UNOCCUPIED, 50000L)
            ColorSlice cs2 = slice(OPERATIONAL, 40000L)
            ColorSlice cs3 = slice(MODERATE_COOLING, 10000L)
            ColorSlice cs4 = slice(MODERATE_HEATING, 70000L)
            ColorSlice cs5 = slice(SPECKLED_GREEN, 30000L)
            ColorSlice cs6 = slice(COOLING_ALARM, 40000L)
            ColorSlice cs7 = slice(HEATING_ALARM, 20000L)
            ColorSlice cs8 = slice(UNKNOWN, 40000L)

            ColorPie pie = new ColorPie([cs1, cs2, cs3, cs4, cs5, cs6, cs7, cs8], 300000L, 5) // five hours, five equipment
        when: "pie calculates satisfaction"
            double satisfaction = pie.getSatisfaction();
        then:
            Math.round(satisfaction) == 100 * (50000L + 40000L + 30000L) / 300000L
    }

    def "No satisfaction colors"()
    {
        given:
            ColorSlice cs1 = slice(MODERATE_COOLING, 50000L)
            ColorSlice cs2 = slice(MODERATE_HEATING, 40000L)
            ColorSlice cs3 = slice(COOLING_ALARM, 10000L)
            ColorSlice cs4 = slice(HEATING_ALARM, 70000L)
            ColorSlice cs5 = slice(UNKNOWN, 30000L)

            ColorPie pie = new ColorPie([cs1, cs2, cs3, cs4, cs5], 200000L, 5)
        when:
            double satisfaction = pie.getSatisfaction()
        then:
            Math.round(satisfaction) == 0
    }

    def "only satisfaction colors"()
    {
        given:
            ColorSlice cs1 = slice(UNOCCUPIED, 50000L)
            ColorSlice cs2 = slice(OPERATIONAL, 40000L)
            ColorSlice cs3 = slice(SPECKLED_GREEN, 20000L)

            ColorPie pie = new ColorPie([cs1, cs2, cs3], 110000, 5)
        when:
            double satisfaction = pie.getSatisfaction()
        then:
            Math.round(satisfaction) == 100.0


    }

    def "only one equipment" ()
    {
        given:
            ColorSlice cs1 = slice(MODERATE_COOLING, 50000L)
            ColorSlice cs2 = slice(MODERATE_HEATING, 40000L)
            ColorSlice cs3 = slice(OPERATIONAL, 10000L)
            ColorSlice cs4 = slice(HEATING_ALARM, 70000L)
            ColorSlice cs5 = slice(UNKNOWN, 30000L)

            ColorPie pie = new ColorPie([cs1, cs2, cs3, cs4, cs5], 200000L, 1)
        when:
            double satisfaction = pie.getSatisfaction()
        then:
            Math.round(satisfaction) == 100 * (10000L /  200000L)
    }

    def "given no equipment" ()
    {
        given:
            ColorSlice cs1 = slice(MODERATE_COOLING, 50000L)
            ColorSlice cs2 = slice(MODERATE_HEATING, 40000L)
            ColorSlice cs3 = slice(OPERATIONAL, 10000L)
            ColorSlice cs4 = slice(HEATING_ALARM, 70000L)
            ColorSlice cs5 = slice(UNKNOWN, 30000L)

            ColorPie pie = new ColorPie([cs1, cs2, cs3, cs4, cs5], 200000L, 0)
        when:
            double satisfaction = pie.getSatisfaction()
        then:
            Double.isNaN(satisfaction)
    }

    def "given negative total time"()
    {
        given:
            ColorSlice cs1 = slice(OPERATIONAL, 80000L)
            ColorSlice cs2 = slice(UNKNOWN, 20000L)
            ColorSlice cs3 = slice(COOLING_ALARM, 50000L)

            ColorPie pie = new ColorPie([cs1, cs2, cs3], -150000L, 2)
        when:
            double satisfaction = Math.round(pie.getSatisfaction())
        then: "negative time is the same as positive time"
            satisfaction == Math.round(100 * 80000L / 150000L)
            // result is due to pie.getSatisfaction's happyPercent and unhappyPercent both being negative
            // therefore when divided, the result is the same.
    }

    def "given no time" ()
    {
        given:
            ColorSlice cs1 = slice(OPERATIONAL, 80000L)
            ColorSlice cs2 = slice(UNKNOWN, 20000L)

            ColorPie pie = new ColorPie([cs1, cs2], 0L, 2)
        when:
            double satisfaction = pie.getSatisfaction()
        then: "Divide by zero"
            Double.isNaN(satisfaction)
    }

    def "given null ColorSlice collection"()
    {
        when:
            new ColorPie(null, 50000L, 2)
        then:
            thrown(NullPointerException)
    }
}

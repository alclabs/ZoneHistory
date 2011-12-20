package com.controlj.addon.zonehistory

import com.controlj.green.addonsupport.access.EquipmentColor
import spock.lang.Specification
import static com.controlj.green.addonsupport.access.EquipmentColor.*

class ColorSliceTest  extends Specification
{
    def slice(EquipmentColor e, long time)
    {
        ColorSlice colorSlice = new ColorSlice(e)
        colorSlice.addTimeInColor(time)
        colorSlice
    }
    def "Init ColorSlice and add some positive time" ()
    {
        given: "New slice and correct time"
            ColorSlice cs = slice(OPERATIONAL, 5000L)
        when:
            double percent = Math.round(cs.getPercentTimeInColor(10000L))
        then: "determine if percent is correct"
            percent == Math.round(100 * 5000L / 10000L)
    }

    def "Zero slice time"()
    {
        given:
            ColorSlice cs = slice(OPERATIONAL, 0L)
        when:
            double percent = Math.round(cs.getPercentTimeInColor(10000L))
        then: "determine if percent is zero"
            percent == 0
    }
}

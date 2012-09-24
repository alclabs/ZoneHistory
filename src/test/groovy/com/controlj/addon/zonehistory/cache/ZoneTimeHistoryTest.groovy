package com.controlj.addon.zonehistory.cache

import spock.lang.Specification

/**
 * 
 */
class ZoneTimeHistoryTest extends Specification {
    def "get midnight today"() {
        setup:
        Calendar today = new GregorianCalendar();

        when:
        Calendar test = new GregorianCalendar();
        test.setTime(ZoneTimeHistory.getMidnightToday())

        then:
        test.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        test.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        test.get(Calendar.HOUR_OF_DAY) == 0
        test.get(Calendar.MINUTE) == 0
        test.get(Calendar.SECOND) == 0
        test.get(Calendar.MILLISECOND) == 0
    }
}

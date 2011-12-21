package com.controlj.addon.zonehistory.cache

import spock.lang.Specification

/**
 * 
 */
class DateRangeTest extends Specification {
    def date(int year, int month, int day, int hour, int minute, int second) { new Date(year-1900, month, day, hour, minute, second) }

    def "test rounding"() {
        expect:
        DateRange.roundDown(date(1990, 2, 1, 4, 20, 37)) == date(1990, 2, 1, 4, 15, 0)
        DateRange.roundDown(date(1996, 4, 27, 3, 14, 12)) == date(1996, 4, 27, 3, 0, 0)
    }

    def "equals with rounding"() {
        expect:
        new DateRange(date(1990, 1, 2, 3, 4, 5), date(1990, 1, 2, 8, 9, 10)) ==
                new DateRange(date(1990, 1, 2, 3, 2, 1), date(1990, 1, 2, 8, 14, 1))
    }

    def "hash with rounding"() {
        expect:
        new DateRange(date(1990, 1, 2, 3, 4, 5), date(1990, 1, 2, 8, 9, 10)).hashCode() ==
                new DateRange(date(1990, 1, 2, 3, 2, 1), date(1990, 1, 2, 8, 5, 1)).hashCode()
    }

}

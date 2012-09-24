package com.controlj.addon.zonehistory.cache

import spock.lang.Specification
import com.controlj.green.addonsupport.access.Location

/**
 * 
 */
class ZoneHistoryCacheTest extends Specification {
    Location loc1 = Mock();
    Location loc2 = Mock();
    Location loc3 = Mock();


    def setup() {
        ZoneHistoryCache.SATISFACTION.reset();
    }

    def "getOfficialZoneHistory returns canonical history"() {
        loc1.getTransientLookupString() >> "loc1"
        loc2.getTransientLookupString() >> "loc2"

        when: "get two different ZoneTimeHistory with same loc"
            ZoneTimeHistory hist1 = new ZoneTimeHistory(loc1)
            ZoneTimeHistory hist1_2 = new ZoneTimeHistory(loc1)
            ZoneTimeHistory result1 = ZoneHistoryCache.SATISFACTION.getOfficialZoneHistory(hist1)
            ZoneTimeHistory result1_2 = ZoneHistoryCache.SATISFACTION.getOfficialZoneHistory(hist1_2)

        then: "always get the same one (first)"
            result1.is(result1_2)

        when: "try a different zone history, then the original"
            ZoneTimeHistory hist2 = new ZoneTimeHistory(loc2)
            ZoneTimeHistory result2 = ZoneHistoryCache.SATISFACTION.getOfficialZoneHistory(hist2)
            result1_2 = ZoneHistoryCache.SATISFACTION.getOfficialZoneHistory(hist1_2)

        then: "get back second, then first again"
            result1.is(result1_2)
            result2.getEquipmentColorLookupString() == loc2.transientLookupString
    }

    def "descendant list works"() {
        setup:
        loc1.getTransientLookupString() >> "loc1"
        loc2.getTransientLookupString() >> "loc2"
        loc3.getTransientLookupString() >> "loc3"
        def histories1 = [new ZoneTimeHistory(loc1), new ZoneTimeHistory(loc2)]
        def histories2 = [new ZoneTimeHistory(loc1), new ZoneTimeHistory(loc2)]

        when: "add histories then read them back"
        def initial = ZoneHistoryCache.SATISFACTION.getDescendantZoneHistories(loc3)
        ZoneHistoryCache.SATISFACTION.addZoneTimeHistory(loc3, histories1)
        def result = ZoneHistoryCache.SATISFACTION.getDescendantZoneHistories(loc3)

        then: "initial read is null, then read set value back"
        initial == null
        result == histories1

        when: "Add another set to same location and read it back"
        ZoneHistoryCache.SATISFACTION.addZoneTimeHistory(loc3, histories2)
        result = ZoneHistoryCache.SATISFACTION.getDescendantZoneHistories(loc3)

        then: "result is original"
        result == histories1
    }
}

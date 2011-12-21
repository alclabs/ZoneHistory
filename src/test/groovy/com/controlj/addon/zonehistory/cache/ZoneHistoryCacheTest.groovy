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
        ZoneHistoryCache.INSTANCE.reset();
    }

    def "getOfficialZoneHistory returns canonical history"() {
        loc1.getTransientLookupString() >> "loc1"
        loc2.getTransientLookupString() >> "loc2"

        when: "get two different ZoneHistory with same loc"
            ZoneHistory hist1 = new ZoneHistory(loc1)
            ZoneHistory hist1_2 = new ZoneHistory(loc1)
            ZoneHistory result1 = ZoneHistoryCache.INSTANCE.getOfficialZoneHistory(hist1)
            ZoneHistory result1_2 = ZoneHistoryCache.INSTANCE.getOfficialZoneHistory(hist1_2)

        then: "always get the same one (first)"
            result1.is(result1_2)

        when: "try a different zone history, then the original"
            ZoneHistory hist2 = new ZoneHistory(loc2)
            ZoneHistory result2 = ZoneHistoryCache.INSTANCE.getOfficialZoneHistory(hist2)
            result1_2 = ZoneHistoryCache.INSTANCE.getOfficialZoneHistory(hist1_2)

        then: "get back second, then first again"
            result1.is(result1_2)
            result2.getEquipmentColorLookupString() == loc2.transientLookupString
    }

    def "descendant list works"() {
        setup:
        loc1.getTransientLookupString() >> "loc1"
        loc2.getTransientLookupString() >> "loc2"
        loc3.getTransientLookupString() >> "loc3"
        def histories1 = [new ZoneHistory(loc1), new ZoneHistory(loc2)]
        def histories2 = [new ZoneHistory(loc1), new ZoneHistory(loc2)]

        when: "add histories then read them back"
        def initial = ZoneHistoryCache.INSTANCE.getDescendantZoneHistories(loc3)
        ZoneHistoryCache.INSTANCE.addDescendantZoneHistories(loc3, histories1)
        def result = ZoneHistoryCache.INSTANCE.getDescendantZoneHistories(loc3)

        then: "initial read is null, then read set value back"
        initial == null
        result == histories1

        when: "Add another set to same location and read it back"
        ZoneHistoryCache.INSTANCE.addDescendantZoneHistories(loc3, histories2)
        result = ZoneHistoryCache.INSTANCE.getDescendantZoneHistories(loc3)

        then: "result is original"
        result == histories1
    }
}
